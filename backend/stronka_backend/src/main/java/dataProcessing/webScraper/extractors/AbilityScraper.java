package dataProcessing.webScraper.extractors;

import dataProcessing.DTOBuilders;
import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.AbilityService;
import dataProcessing.webScraper.ScraperNodeVisitors;
import dataProcessing.webScraper.WikimediaScraper;
import dataProcessing.webScraper.exceptions.MissingImageException;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import dataProcessing.webScraper.exceptions.MissingTextException;
import dataProcessing.webScraper.extractors.utils.ExtractionUtils;
import dataProcessing.webScraper.extractors.utils.ScraperConstants;
import dataProcessing.webScraper.utils.ImageScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AbilityScraper
{
    private final AbilityService abilityService;

    public AbilityScraper(AbilityService abilityService)
    {
        this.abilityService = abilityService;
    }

    // TODO jeżeli chcę dodać wstawianie umiejętności do db, musiałbym tutaj nad tym operować
    public void scrapeIDAbilityData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        Elements largeSelector = htmlContent.select(".tabber .tabber__section [id^=Skill_1-], "
                + ".tabber .tabber__section [id^=Skill_1_]:not([id$=label]), "
                + ".tabber .tabber__section [id^=Skill_2-], "
                + ".tabber .tabber__section [id^=Skill_2_]:not([id$=label]), "
                + ".tabber .tabber__section [id^=Skill_3-], "
                + ".tabber .tabber__section [id^=Skill_3_]:not([id$=label]), "
                + ".tabber .tabber__section [id^=Defense-], "
                + ".tabber .tabber__section [id^=Defense_]:not([id$=label])");
        for (Element element : largeSelector)
        {
            // Elementy -label nas nie interesują, jak i nie chcemy kontynerów
            if (element.id().endsWith("-label") || !element.select(".tabber").isEmpty())
            {
                continue;
            }
            builder.addAbility(processSingleAbility(element, new DTOBuilders.AbilityDataBuilder()));
        }
    }

    // TODO dodaj zbieranie status effectów
    public UUID processSingleAbility(Element abilityContainer, DTOBuilders.AbilityDataBuilder abilityBuilder)
    {
        return processSingleAbility(abilityContainer, abilityBuilder, false);
    }

    public UUID processSingleAbility(Element abilityContainer, DTOBuilders.AbilityDataBuilder abilityBuilder, boolean isEgo)
    {
        // Żebym wiedział, co się dzieje
        String skillSlot = abilityContainer.id();
        if (!isEgo) {
            abilityBuilder.setSkillSlot(skillSlot);
        }
        log.info("PROCESSING: {}", skillSlot);

        // Zwraca pusty pojemnik, jeżeli nie istnieje
        Elements columns = Optional.ofNullable(abilityContainer.select("table tr").first())
                .map(row -> row.select("td"))
                .orElseThrow(() -> new MissingSectionException("columns"));

        // Z racji, że wiemy, że zawsze będziemy mieć daną kolejność kolumn,
        // możemy działać po kolejności pojawienia się
        Element leftAbilityPanel = ExtractionUtils.extractElementFromSpecifiedIndex(columns, 0, "leftAbilityPanel");

        scrapeSinAffinity(abilityBuilder, leftAbilityPanel);
        scrapeSkillIcon(abilityBuilder, leftAbilityPanel);
        scrapePower(abilityBuilder, leftAbilityPanel);
        scrapeDamageType(abilityBuilder, leftAbilityPanel);

        Element rightAbilityPanel = ExtractionUtils.extractElementFromSpecifiedIndex(columns, 1, "rightAbilityPanel");

        scrapeCoinCount(abilityBuilder, rightAbilityPanel);
        scrapeAbilityName(abilityBuilder, rightAbilityPanel);
        scrapeAttackWeight(abilityBuilder, rightAbilityPanel);
        scrapeOffenseLevel(abilityBuilder, rightAbilityPanel);
        scrapeEffects(abilityBuilder, rightAbilityPanel);

        // TODO Powinienem tutaj bezpośrednio budować umiejętnośc do DB, a do DTO zwykle dodać jej klucz główny
        ScraperDataDTOs.Ability builtAbility = abilityBuilder.buildAbilityData();
        return abilityService.saveNewAbility(builtAbility);
    }

    // FIXME Nie ma base effects dla wielu EGO
    //  Spowodowane tym że mają w tekście znak + albo - ?
    public static void scrapeEffects(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        ScraperNodeVisitors.AbilityResult result = ScraperNodeVisitors.extractAbility(rightAbilityPanel);

        abilityBuilder.setBaseEffects(result.baseEffects());
        log.info("Base effects:");
        if (result.baseEffects().isEmpty())
        {
            log.info("- No Base Effects");
        }
        else
        {
            result.baseEffects().forEach(effect -> log.info("- {}", effect));
        }

        abilityBuilder.setCoinEffects(result.coinEffects());
        abilityBuilder.setStatusEffects(result.statusEffects());
        result.coinEffects().forEach((coinNum, effects) ->
        {
            log.info("Coin {} effects: ", coinNum);
            effects.forEach(effect -> log.info("- {}", effect));
        });
    }

    public static void scrapeOffenseLevel(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        String offenseLevel = rightAbilityPanel.textNodes().stream()
                .map(tn -> tn.text().trim())
                .filter(t -> t.matches(".*[+-]\\d+.*"))
                .findFirst()
                .map(t -> t.replaceAll(".*([+-]\\d+).*", "$1"))
                .orElseThrow(() -> new MissingTextException("offenseLevel"));
        abilityBuilder.setOffenseLevel(offenseLevel);
        log.info("Offense level: {}", offenseLevel);
    }

    public static void scrapeAttackWeight(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        Element attackWeightElement = rightAbilityPanel.selectFirst("font");
        if (attackWeightElement == null)
        {
            throw new MissingTextException("attackWeightElement");
        }
        String attackWeight = attackWeightElement.text().replace("Atk Weight ", "").trim();
        abilityBuilder.setAttackWeight(attackWeight);
        log.info("Attack weight: {}", attackWeight);
    }

    public static void scrapeAbilityName(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        Element abilityNameElement = rightAbilityPanel.selectFirst("div span");
        if (abilityNameElement == null)
        {
            throw new MissingTextException("abilityNameElement");
        }
        String abilityName = abilityNameElement.text();
        abilityBuilder.setAbilityName(abilityName);
        log.info("Ability name: {}", abilityName);
    }

    public static void scrapeCoinCount(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        int coinCount = 0;
        for (Element image : rightAbilityPanel.select("img"))
        {
            if (image.attr("alt").contains("Coin"))
            {
                coinCount += 1;
            }
            else
            {
                break;
            }
        }
        abilityBuilder.setCoinCount(coinCount);
        log.info("Coin count: {}", coinCount);
    }

    public static void scrapeDamageType(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        // w Jsoup '>' oznacza bezpośrednie dziecko
        Element damageTypeElement = leftAbilityPanel.selectFirst("> img");
        if (damageTypeElement == null)
        {
            throw new MissingImageException("damageTypeElement");
        }
        String damageType = damageTypeElement.attr("alt");
        abilityBuilder.setDamageType(damageType);
        log.info("Damage type: {}", damageType.replace(".png", ""));
    }

    public static void scrapePower(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        Elements abilityPowers = leftAbilityPanel.select("b");
        Element basePowerElement = ExtractionUtils.extractElementFromSpecifiedIndex(abilityPowers, 0, "basePowerElement");
        abilityBuilder.setBasePower(Integer.parseInt(basePowerElement.text()));
        log.info("Base power: {}", basePowerElement.text());

        Element coinPowerElement = ExtractionUtils.extractElementFromSpecifiedIndex(abilityPowers, 1, "coinPowerElement");
        abilityBuilder.setCoinPower(coinPowerElement.text());
        log.info("Coin power: {}", coinPowerElement.text());
    }

    public static void scrapeSkillIcon(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=Icon.png], img[alt$=Skill.png]");
        if (skillIcon == null)
        {
            skillIcon = leftAbilityPanel.selectFirst(ScraperConstants.whitelistSelector);
        }
        if (skillIcon == null)
        {
            throw new MissingImageException("skillIcon");
        }
        ImageScraper.scrapeImageURL(skillIcon);
        String skillIconFile = skillIcon.attr("alt");
        skillIconFile = skillIconFile.replaceAll("[<>:\"/\\\\|?*]", "");
        abilityBuilder.setSkillIconFile(skillIconFile);
        log.info(skillIconFile.replace(".png", ""));
    }

    public static void scrapeSinAffinity(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        // W razie, że nic nie dostaniemy, lepsze coś niż null
        String altText = "None";
        for (Element image : leftAbilityPanel.select("img"))
        {
            altText = image.attr("alt");
            if (ScraperConstants.iconWhiteList.contains(altText))
            {
                abilityBuilder.setSinAffinity(altText);
                log.info("Sin affinity: {}", altText.replace(".png", ""));
                break;
            }
        }
    }
}