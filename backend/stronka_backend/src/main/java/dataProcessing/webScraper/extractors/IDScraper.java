package dataProcessing.webScraper.extractors;

import dataProcessing.DTOBuilders;
import dataProcessing.webScraper.WikimediaScraper;
import dataProcessing.webScraper.enums.Rarity;
import dataProcessing.webScraper.exceptions.MissingImageException;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import dataProcessing.webScraper.exceptions.MissingTextException;
import dataProcessing.webScraper.extractors.utils.ExtractionUtils;
import dataProcessing.webScraper.utils.ImageScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class IDScraper
{
    private final GenericScrapers genericScrapers;

    public IDScraper(GenericScrapers genericScrapers)
    {
        this.genericScrapers = genericScrapers;
    }

    // FIXME Do poprawnienia jeszcze kilka rzeczy w LCB Sinner
    //  Wyrzuca się na https://limbuscompany.wiki.gg/wiki/Cheery_Chickies_Class_Captain_Yi_Sang
    public DTOBuilders.IDDataBuilder scrapeGeneralIDData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        String[] idResistances = {"Slash", "Pierce", "Blunt"};
        genericScrapers.scrapeName(htmlContent, builder);
        Element smallSelector;

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");

        // Pobieramy obrazek i od razu ustawiamy nazwę
        if (smallSelector == null)
        {
            String url = htmlContent.location();
            if (url.contains("LCB_Sinner"))
            {
                smallSelector = htmlContent.selectFirst(".ABMobile div[style*=margin:4px;text-align:center] a img");
            }
            else
            {
                return null;
            }
        }
        String portraitName = Optional.ofNullable(smallSelector)
                .map(element -> element.attr("alt"))
                .orElseThrow(() -> new MissingImageException("portraitName"));
        log.info("Portrait: {}", portraitName);
        builder.setPortraitFile(portraitName);
        ImageScraper.scrapeImageURL(smallSelector);
        Elements largeSelector = htmlContent.select("#General_Info-0 table tr");
        for (Element row : largeSelector)
        {
            Elements specifiedRow = row.select("td");
            int specifiedRowSize = specifiedRow.size();
            switch (specifiedRowSize)
            {
                case 1 -> scrapeStaggerThresholds(builder, specifiedRow);
                case 2 -> scrapeTraits(builder, specifiedRow);
                case 3 -> {
                    if (!specifiedRow.getFirst().text().isEmpty()) {
                        genericScrapers.parseResistances(specifiedRow.text(), idResistances, builder);
                    }
                }
                case 4 -> scrapeTypeData(builder, specifiedRow);
                case 6 -> scrapeBaseStats(builder, specifiedRow);
            }
        }
        return builder;
    }

    public void scrapeStaggerThresholds(DTOBuilders.IDDataBuilder builder, Elements specifiedRow)
    {
        Element specificatorHelper;
        specificatorHelper = ExtractionUtils.extractElementFromSpecifiedIndex(specifiedRow, 0, "staggerThreshold");
        Elements staggerThresholds = specificatorHelper.getElementsByTag("font");
        log.info("Stagger Threshholds: ");
        for (Element staggerThreshold : staggerThresholds)
        {
            builder.addStaggerThreshold(staggerThreshold.text());
            log.info(staggerThreshold.text());
        }
    }

    public void scrapeTraits(DTOBuilders.IDDataBuilder builder, Elements specifiedRow)
    {
        Element specificatorHelper;
        specificatorHelper = ExtractionUtils.extractElementFromSpecifiedIndex(specifiedRow, 1, "unitTrait");
        Elements unitTraits = specificatorHelper.getElementsByTag("b");
        log.info("Traits: ");
        for (Element unitTrait : unitTraits) {
            builder.addTrait(unitTrait.text());
            log.info(unitTrait.text());
        }
    }

    public void scrapeTypeData(DTOBuilders.IDDataBuilder builder, Elements specifiedRow)
    {
        String rowType = "ERROR!";
        String rowText = ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 0, "rowText");
        if(rowText.contains("Rarity"))
        {
            rowType = "RarityWorld";
        }
        else if (rowText.contains("Season"))
        {
            rowType = "SeasonRelease";
        }
        if (rowType.equals("RarityWorld"))
        {
            String rarityIMG = ExtractionUtils.extractAltFromSpecifiedIndex(specifiedRow, 1, "rarityIMG");
            log.info("Rarity : {}", rarityIMG);
            builder.setRarity(Rarity.rarityParser(rarityIMG));

            Element worldIMGURL = ExtractionUtils.extractElementFromSpecifiedIndex(specifiedRow, 3, "worldIMGURL").selectFirst("img");
            String worldIMG = ExtractionUtils.extractAltFromSpecifiedIndex(specifiedRow, 3, "worldIMG");
            // TODO Niepotrzebna redundancja danych, pozbyć się nazwy, zostawić plik
            builder.setWorldFile(worldIMG);
            builder.setWorld(worldIMG.replace("Icon.png", ""));
            log.info("World : {}", worldIMG);
            ImageScraper.scrapeImageURL(worldIMGURL);
        }
        else if (rowType.equals("SeasonRelease"))
        {
            String season = ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 1, "season");
            builder.setSeason(season);
            log.info("Season : {}", season);

            String release = ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 3, "release");
            builder.setReleaseDate(release);
            log.info("Release : {}", release);
        }
        else
        {
            throw new MissingSectionException("typeData");
        }
    }

    public void scrapeBaseStats(DTOBuilders.IDDataBuilder builder, Elements specifiedRow)
    {
        int health = Integer.parseInt(ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 1, "health"));
        builder.setHealth(health);
        log.info("Health: {}", health);

        String speed = ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 3, "speed");
        builder.setSpeed(speed);
        log.info("Speed: {}", speed);

        int defenseLevel = Integer.parseInt(ExtractionUtils.extractTextFromSpecifiedIndex(specifiedRow, 5, "defenseLevel"));
        builder.setDefenseLevel(defenseLevel);
        log.info("Defense level: {}", defenseLevel);
    }
    // TODO Stworzyć osobnę DTO dla sanity
    public void scrapeSanityData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        Elements sanitySelector = htmlContent.select("#Sanity-0 table tr td div");
        for (Element sanitySection : sanitySelector)
        {
            String headerText = sanitySection.select("ul span").text();
            Elements sanityEffects = sanitySection.select("ul li");
            if (headerText.contains("increasing Sanity"))
            {
                log.info("Sanity+:");
                for (Element sanityEffect : sanityEffects)
                {
                    builder.addPositiveSanityEffect(sanityEffect.text().trim());
                    log.info(sanityEffect.text());

                }
            } else if (headerText.contains("decreasing Sanity"))
            {
                log.info("Sanity-:");
                for (Element sanityEffect : sanityEffects)
                {
                    builder.addNegativeSanityEffect(sanityEffect.text().trim());
                    log.info(sanityEffect.text());
                }
            }
        }
    }
}