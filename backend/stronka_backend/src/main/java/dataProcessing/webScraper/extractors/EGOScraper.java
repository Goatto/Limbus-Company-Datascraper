package dataProcessing.webScraper.extractors;

import dataProcessing.DTOBuilders;
import dataProcessing.webScraper.WikimediaScraper;
import dataProcessing.webScraper.enums.ThreatLevel;
import dataProcessing.webScraper.exceptions.MissingImageException;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import dataProcessing.webScraper.extractors.utils.ExtractionUtils;
import dataProcessing.webScraper.extractors.utils.ScraperConstants;
import dataProcessing.webScraper.utils.ImageScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class EGOScraper
{
    private final GenericScrapers genericScrapers;
    private final AbilityScraper abilityScraper;

    public EGOScraper(GenericScrapers genericScrapers, AbilityScraper abilityScraper)
    {
        this.genericScrapers = genericScrapers;
        this.abilityScraper = abilityScraper;
    }

    // TODO Rozbić na mniejsze metody, pamiętaj o SINGLE RESPONSIBILITY PRINCIPLE
    public Object scrapeEGOAbilities(Document htmlContent, DTOBuilders.EGODataBuilder builder) {
        genericScrapers.scrapeName(htmlContent, builder);
        Element genericInformation = Optional.of(htmlContent)
                .map(content -> content.selectFirst(".mw-body-content"))
                .orElseThrow(() -> new MissingSectionException("genericInformation"));

        boolean hasNoCorrosion = false;
        Element genericInformationCost;

        Element overclockCost = genericInformation.selectFirst("b:contains(Cost (Overclock))");

        if (overclockCost != null) {
            genericInformationCost = overclockCost.closest("tbody");
        } else {
            Element standardCost = genericInformation.selectFirst("b:contains(Cost)");
            if (standardCost != null) {
                hasNoCorrosion = true;
                genericInformationCost = standardCost.closest("tbody");
            } else {
                return null;
            }
        }

        scrapeEgoGenericInformation(builder, genericInformation, hasNoCorrosion);

        Elements genericRows = Optional.ofNullable(genericInformationCost)
                .map(element -> element.getElementsByTag("tr"))
                .orElseThrow(() -> new MissingSectionException("genericRows"));

        boolean isOverclocked = false;
        for (Element row : genericRows) {
            if (row.text().contains("(Overclock)")) {
                isOverclocked = true;
                continue;
            }

            Elements specifiedRow = row.select("td");

            if (specifiedRow.size() < 2) {
                continue;
            }

            Element headerRow = ExtractionUtils.extractElementFromSpecifiedIndex(specifiedRow, 0, "headerRow");
            Element dataRow = ExtractionUtils.extractElementFromSpecifiedIndex(specifiedRow, 1, "dataRow");

            if (dataRow.text().isEmpty()) {
                return null;
            }
            if (headerRow.text().contains("Sanity")) {
                scrapeSanityCost(builder, dataRow, isOverclocked);
            }
            // Edge-case 'Affinity' dla base EGO
            else if (headerRow.text().contains("E.G.O Resources") || headerRow.text().contains("Affinity")) {
                scrapeEGOResourceCost(builder, dataRow, isOverclocked);
            }
        }
        Element genericInformationResistances = Optional.ofNullable(
                        genericInformation.selectFirst("b:contains(Resistances)"))
                .map(container -> container.closest("tbody"))
                .orElseThrow(() -> new MissingSectionException("genericInformationResistances"));
        Element row = genericInformationResistances.child(1);

        genericScrapers.parseResistances(row.text(), ScraperConstants.egoResistances, builder);
        // TODO Znormalizować zapis skillslotow
        scrapeEGOInformation(htmlContent, builder, hasNoCorrosion);
        scrapeEGOPortraits(htmlContent, builder, hasNoCorrosion);

        return true;
    }// TODO Pomyśl czy nie lepiej stworzyć nowego abilityBuildera zamiast robić to tak

    public void scrapeEGOInformation(Document htmlContent, DTOBuilders.EGODataBuilder builder,
                                     boolean hasNoCorrosion) {
        DTOBuilders.AbilityDataBuilder abilityBuilder = new DTOBuilders.AbilityDataBuilder();
        Element awakeningInformation;
        abilityBuilder.setSkillSlot("Skill_1");
        if (!hasNoCorrosion) {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-awakening div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("awakeningInformation"));
            builder.addAbility(abilityScraper.processSingleAbility(awakeningInformation, abilityBuilder, true));

            abilityBuilder.setSkillSlot("Skill_2");
            Element corrosionInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("corrosionInformation"));
            builder.addAbility(abilityScraper.processSingleAbility(corrosionInformation, abilityBuilder, true));
        } else {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("awakeningInformation"));
            builder.addAbility(abilityScraper.processSingleAbility(awakeningInformation, abilityBuilder, true));
        }
    }

    public static void scrapeEGOPortraits(Document htmlContent, DTOBuilders.EGODataBuilder builder,
                                          boolean hasNoCorrosion) {
        Element awakeningPortrait;
        if (!hasNoCorrosion) {
            awakeningPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-awakening div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("awakeningPortrait"));

            Element corrosionPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("corrosionPortrait"));

            builder.setCorrededPortraitFile(corrosionPortrait.attr("alt"));
            ImageScraper.scrapeImageURL(corrosionPortrait);
        } else {
            awakeningPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("awakeningPortrait"));
        }
        builder.setPortraitFile(awakeningPortrait.attr("alt"));
    }

    public static void scrapeSanityCost(DTOBuilders.EGODataBuilder builder, Element dataRow, boolean isOverclocked) {
        int sanityValue = Integer.parseInt(dataRow.text());
        if (isOverclocked) {
            log.info("Corrosion sanity cost: {}", sanityValue);
            builder.setCorrosionSanityCost(sanityValue);
        } else {
            log.info("Awakening sanity cost: {}", sanityValue);
            builder.setAwakenSanityCost(sanityValue);
        }
    }

    public static void scrapeEGOResourceCost(DTOBuilders.EGODataBuilder builder, Element dataRow, boolean isOverclocked) {
        StringBuilder resourcesEGO = new StringBuilder();

        Map<String, Integer> useCost = new LinkedHashMap<String, Integer>();
        String sinType = "";
        String sinCost;
        for (Node dataNode : dataRow.childNodes()) {
            sinCost = "";
            if (dataNode instanceof Element nodeElement) {
                if (nodeElement.tagName().equals("img")) {
                    sinType = nodeElement.attr("alt");
                    resourcesEGO.append("[").append(sinType).append("]").append(" ");
                } else if (nodeElement.tagName().equals("span")) {
                    sinCost = nodeElement.text().replaceAll("\\D", "");
                }
            } else if (dataNode instanceof TextNode) {
                sinCost = ((TextNode) dataNode).text().replaceAll("\\D", "");
            }
            if (!sinCost.isEmpty()) {
                resourcesEGO.append(sinCost).append(" ");
                useCost.put(sinType, Integer.parseInt(sinCost));
            }
        }
        if (isOverclocked) {
            log.info("Corrosion E.G.O Resources: {}", resourcesEGO.toString().trim());
            builder.setCorrosionSinCost(useCost);
        } else {
            log.info("Awakening E.G.O Resources: {}", resourcesEGO.toString().trim());
            builder.setAwakenSinCost(useCost);
        }
    }

    public static void scrapeEgoGenericInformation(DTOBuilders.EGODataBuilder builder, Element genericInformation, boolean hasNoCorrosion) {
        Elements genericRows;
        Element genericInformationInfo = Optional.ofNullable(genericInformation)
                .map(info -> info.selectFirst("b:contains(Info)"))
                .map(container -> container.closest("tbody"))
                .orElseThrow(() -> new MissingSectionException("genericInformationInfo"));
        genericRows = genericInformationInfo.getElementsByTag("tr");

        for (Element row : genericRows) {
            Elements specifiedRow = row.select("td");
            if (row.text().trim().isEmpty()) {
                continue;
            }
            if (specifiedRow.selectFirst("td:contains(Risk Level)") != null) {
                String riskLevel = ExtractionUtils.extractAltFromSpecifiedIndex(specifiedRow, 1, "riskLevel");
                builder.setThreatLevel(ThreatLevel.threatLevelParser(riskLevel));
                log.info("Risk Level: {}", riskLevel);

                String season = hasNoCorrosion ? "Season 0" : ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 3, "season");
                builder.setSeason(season);
                log.info("Season: {}", season);
            } else if (specifiedRow.selectFirst("td:contains(Affinity)") != null) {

                String affinity = ExtractionUtils.extractAltFromSpecifiedIndex(specifiedRow, 1, "affinity");
                builder.setSinAffinity(affinity);
                log.info("Affinity: {}", affinity);

                String release = hasNoCorrosion ? "Day 1" : ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 3, "release");
                builder.setReleaseDate(release);
                log.info("Release: {}", release);

                if (hasNoCorrosion) {
                    break;
                }
            } else if (specifiedRow.selectFirst("td:contains(Abnormality)") != null) {
                String abnormality = ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 1, "abnormality");
                builder.setAbnormality(abnormality);
                log.info("Abnormality: {}", abnormality);
            }
        }
    }
}