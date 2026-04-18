package dataProcessing.webScraper;

import dataProcessing.DTOBuilders;
import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.AbilityService;
import dataProcessing.services.PassiveService;
import dataProcessing.webScraper.enums.PassiveCategory;
import dataProcessing.webScraper.enums.Rarity;
import dataProcessing.webScraper.enums.ThreatLevel;
import dataProcessing.webScraper.exceptions.MissingImageException;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import dataProcessing.webScraper.exceptions.MissingTextException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static dataProcessing.webScraper.utils.ImageScraper.scrapeImageURL;

// TODO Rozbić klasę na pomniejsze
@Slf4j
@Component
public class WikimediaScraper
{
    static Set<String> iconWhiteList = Set.of("Wrath1.png","Wrath2.png","Wrath3.png",
            "Lust1.png", "Lust2.png", "Lust3.png",
            "Sloth1.png", "Sloth2.png", "Sloth3.png",
            "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
            "Gloom1.png", "Gloom2.png", "Gloom3.png",
            "Pride1.png", "Pride2.png", "Pride3.png",
            "Envy1.png", "Envy2.png", "Envy3.png");

    static List<String> sinnerWhiteList = List.of("Yi Sang", "Faust", "Don Quixote", "Ryōshū", "Ryoshu",
            "Meursault", "Hong Lu", "Heathcliff", "Ishmael", "Rodion", "Sinclair", "Outis", "Gregor");

    static String[] egoResistances = {"Wrath", "Lust", "Sloth", "Gluttony", "Gloom", "Pride", "Envy"};

    static String whitelistSelector = sinnerWhiteList.stream()
            .map(sinner -> "img[alt*=\"" + sinner + "\"]")
            .collect(Collectors.joining(", "));

    private final AbilityService abilityService;
    private final PassiveService passiveService;

    public WikimediaScraper(AbilityService abilityService, PassiveService passiveService)
    {
        this.abilityService = abilityService;
        this.passiveService = passiveService;
    }

    // FIXME Do poprawnienia jeszcze kilka rzeczy w LCB Sinner
    public static DTOBuilders.IDDataBuilder scrapeGeneralIDData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        String[] idResistances = {"Slash", "Pierce", "Blunt"};
        scrapeName(htmlContent, builder);
        Element smallSelector;

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");

        // Pobieramy obrazek i od razu ustawiamy nazwę
        if(smallSelector == null)
        {
            String url = htmlContent.location();
            if(url.contains("LCB_Sinner"))
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
        scrapeImageURL(smallSelector);
        Elements largeSelector = htmlContent.select("#General_Info-0 table tr");
        for (Element row : largeSelector)
        {
            Elements specifiedRow = row.select("td");
            int specifiedRowSize = specifiedRow.size();
            switch(specifiedRowSize)
            {
                case 1 -> scrapeStaggerThresholds(builder, specifiedRow);
                case 2 -> scrapeTraits(builder, specifiedRow);
                case 3 ->
                {
                    if(!specifiedRow.getFirst().text().isEmpty())
                    {
                        parseResistances(specifiedRow.text(), idResistances, builder);
                    }
                }
                case 4 -> scrapeTypeData(builder, specifiedRow);
                case 6 -> scrapeBaseStats(builder, specifiedRow);
            }
        }
        return builder;
    }

    private static void scrapeName(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder) {
        Element titleSelector = htmlContent.selectFirst(".mw-page-title-main");
        if(titleSelector == null)
        {
            throw new MissingTextException("titleSelector");
        }
        String unitTitle = titleSelector.text();

        boolean sinnerFound = false;
        for(String sinner : sinnerWhiteList)
        {
            if(unitTitle.contains(sinner))
            {
                log.info("Sinner: {}", sinner);
                builder.setSinnerName(sinner);
                sinnerFound = true;
                builder.setName(unitTitle);
                log.info(unitTitle);
                break;
            }
        }
        if(!sinnerFound)
        {
            scrapeBackupName(htmlContent, builder);
        }
    }

    private static void scrapeBackupName(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        String url = htmlContent.location();
        if(url.contains("/wiki/"))
        {
            url = url.substring(url.lastIndexOf("/wiki/") + 6);
        }

        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String formattedName = decodedUrl.replace("_", " ");
        log.info("BACKUP Title: {}", formattedName);
        builder.setName(formattedName);
        for(String sinner : sinnerWhiteList)
        {
            if(formattedName.contains(sinner))
            {
                log.info("BACKUP Sinner: {}", sinner);
                builder.setSinnerName(sinner);
                break;
            }
        }
    }

    private static void scrapeStaggerThresholds(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        Element specificatorHelper;
        specificatorHelper = extractElementFromSpecifiedIndex(specifiedRow, 0, "staggerThreshold");
        Elements staggerThresholds = specificatorHelper.getElementsByTag("font");
        log.info("Stagger Threshholds: ");
        for(Element staggerThreshold : staggerThresholds)
        {
            builder.addStaggerThreshold(staggerThreshold.text());
            log.info(staggerThreshold.text());
        }
    }

    private static void scrapeTraits(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        Element specificatorHelper;
        specificatorHelper = extractElementFromSpecifiedIndex(specifiedRow, 1, "unitTrait");
        Elements unitTraits = specificatorHelper.getElementsByTag("b");
        log.info("Traits: ");
        for(Element unitTrait : unitTraits)
        {
            builder.addTrait(unitTrait.text());
            log.info(unitTrait.text());
        }
    }

    private static void scrapeTypeData(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        String rowType = "ERROR!";
        String rowText = extractTextFromSpecifiedIndex(specifiedRow, 0, "rowText");
        if(rowText.contains("Rarity"))
        {
            rowType = "RarityWorld";
        }
       else if(rowText.contains("Season"))
        {
            rowType = "SeasonRelease";
        }
        if(rowType.equals("RarityWorld"))
        {
            String rarityIMG = extractAltFromSpecifiedIndex(specifiedRow, 1, "rarityIMG");
            builder.setRarity(Rarity.rarityParser(rarityIMG));
            log.info("Rarity : {}", rarityIMG);

            Element worldIMGURL = extractElementFromSpecifiedIndex(specifiedRow, 3, "worldIMGURL").selectFirst("img");
            String worldIMG = extractAltFromSpecifiedIndex(specifiedRow, 3, "worldIMG");
            // TODO Niepotrzebna redundancja danych, pozbyć się nazwy, zostawić plik
            builder.setWorldFile(worldIMG);
            builder.setWorld(worldIMG.replace("Icon.png", ""));
            log.info("World : {}", worldIMG);
            scrapeImageURL(worldIMGURL);
        }
        else if(rowType.equals("SeasonRelease"))
        {
            String season = extractTextFromSpecifiedIndex(specifiedRow, 1, "season");
            builder.setSeason(season);
            log.info("Season : {}", season);

            String release = extractTextFromSpecifiedIndex(specifiedRow, 3, "release");
            builder.setReleaseDate(release);
            log.info("Release : {}", release);
        }
    }

    private static void scrapeBaseStats(DTOBuilders.IDDataBuilder builder, Elements specifiedRow)
    {
        int health = Integer.parseInt(extractTextFromSpecifiedIndex(specifiedRow, 1, "health"));
        builder.setHealth(health);
        log.info("Health: {}", health);

        String speed = extractTextFromSpecifiedIndex(specifiedRow, 3, "speed");
        builder.setSpeed(speed);
        log.info("Speed: {}", speed);

        int defenseLevel = Integer.parseInt(extractTextFromSpecifiedIndex(specifiedRow, 5, "defenseLevel"));
        builder.setDefenseLevel(defenseLevel);
        log.info("Defense level: {}", defenseLevel);
    }

    // TODO Stworzyć osobnę DTO dla sanity
    public static void scrapeSanityData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        Elements sanitySelector = htmlContent.select("#Sanity-0 table tr td div");
        for (Element sanitySection : sanitySelector)
        {
            String headerText = sanitySection.select("ul span").text();
            Elements sanityEffects = sanitySection.select("ul li");
            if (headerText.contains("increasing Sanity"))
            {
                log.info("Sanity+:");
                for(Element sanityEffect : sanityEffects)
                {
                    builder.addPositiveSanityEffect(sanityEffect.text().trim());
                    log.info(sanityEffect.text());

                }
            }
            else if (headerText.contains("decreasing Sanity"))
            {
                log.info("Sanity-:");
                for(Element sanityEffect : sanityEffects)
                {
                    builder.addNegativeSanityEffect(sanityEffect.text().trim());
                    log.info(sanityEffect.text());

                }
            }
        }
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

    // TODO Dodać dodawanie pasywek do DB
    public void scrapePassiveData(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        Set<PassiveCategory> previousCategories = EnumSet.noneOf(PassiveCategory.class);
        // Ciekawy styl jest z tym b:contains...
        Elements prePassiveHeaders = htmlContent.select("b:contains(Passive), "
                + "b:contains(Combat Passives), b:contains(Support Passive), b:contains(Passives)");

        PassiveCategory passiveCategory;
        for (Element prePassiveHeader : prePassiveHeaders)
        {
            String headerText = prePassiveHeader.text().trim();
            passiveCategory = PassiveCategory.passiveParser(headerText);
            if(passiveCategory == PassiveCategory.UNKNOWN)
            {
                continue;
            }
            // Z racji struktury strony, mamy kilka razy kontenery o tej samej zawartości, z racji,
            // że nie chcę wypisywac ich wszystkich, robimy continue, z jakiejś racji dochodzi do tego tylko dla E.G.O,
            // więc w poniższy warunek jest odpowiedni

            // Set zwraca false, jeżeli metoda add napotka już istniejący element
            if(!previousCategories.add(passiveCategory))
            {
                continue;
            }
            log.info(String.valueOf(passiveCategory));
            Element currentParent = prePassiveHeader.parent();
            // Pozwala nam an operowanie na stronach E.G.O jak i ID
            Element passiveContainer = Optional.ofNullable(currentParent)
                    .map(Element::nextElementSibling)
                    // Jeżeli nie możemy wyłapać następcę dla currentParent,
                    // przechodzimy do jego parenta i szukamy jego następcy
                    .orElseGet(() -> Optional.ofNullable(currentParent)
                            .map(Element::parent)
                            .map(Element::nextElementSibling)
                            // W innym wypadku dajemy pustego diva, żeby program się nie wyrzucił,
                            .orElseThrow(() -> new MissingSectionException("passiveContainer")));
            Elements divPassiveContainers = passiveContainer.select("div[style*=padding:10px]");

            for(Element divPassiveContainer : divPassiveContainers)
            {
                ScraperDataDTOs.Passive passive = ScraperNodeVisitors.extractPassive(divPassiveContainer);
                UUID uuid = passiveService.saveNewPassive(passive);
                switch(passiveCategory)
                {
                    case COMBAT_PASSIVE ->
                    {
                        if(builder instanceof DTOBuilders.HasCombatPassive<?> combatBuilder)
                        {
                            combatBuilder.addCombatPassive(uuid);
                        }
                    }
                    case SUPPORT_PASSIVE ->
                    {
                        if(builder instanceof DTOBuilders.HasSupportPassive<?> supportBuilder)
                        {
                            supportBuilder.addSupportPassive(uuid);
                        }
                    }
                }
            }
        }
    }

    // TODO Rozbić na mniejsze metody, pamiętaj o SINGLE RESPONSIBILITY PRINCIPLE
    public Object scrapeEGOAbilities(Document htmlContent, DTOBuilders.EGODataBuilder builder)
    {
        scrapeName(htmlContent, builder);
        Element genericInformation = Optional.of(htmlContent)
                .map(content -> content.selectFirst(".mw-body-content"))
                .orElseThrow(() -> new MissingSectionException("genericInformation"));

        boolean hasNoCorrosion = false;
        Element genericInformationCost;

        Element overclockCost = genericInformation.selectFirst("b:contains(Cost (Overclock))");

        if (overclockCost != null)
        {
            genericInformationCost = overclockCost.closest("tbody");
        }
        else
        {
            Element standardCost = genericInformation.selectFirst("b:contains(Cost)");
            if (standardCost != null)
            {
                hasNoCorrosion = true;
                genericInformationCost = standardCost.closest("tbody");
            }
            else
            {
                return null;
            }
        }

        scrapeEgoGenericInformation(builder, genericInformation, hasNoCorrosion);

        Elements genericRows = Optional.ofNullable(genericInformationCost)
                .map(element -> element.getElementsByTag("tr"))
                .orElseThrow(() -> new MissingSectionException("genericRows"));

        boolean isOverclocked = false;
        for(Element row : genericRows)
        {
            if(row.text().contains("(Overclock)"))
            {
                isOverclocked = true;
                continue;
            }

            Elements specifiedRow = row.select("td");

            if(specifiedRow.size() < 2)
            {
                continue;
            }

            Element headerRow = extractElementFromSpecifiedIndex(specifiedRow, 0, "headerRow");
            Element dataRow = extractElementFromSpecifiedIndex(specifiedRow, 1, "dataRow");

            if(dataRow.text().isEmpty())
            {
                return null;
            }
            if(headerRow.text().contains("Sanity"))
            {
                scrapeSanityCost(builder, dataRow, isOverclocked);
            }
            // Edge-case 'Affinity' dla base EGO
            else if(headerRow.text().contains("E.G.O Resources") || headerRow.text().contains("Affinity"))
            {
                scrapeEGOResourceCost(builder, dataRow, isOverclocked);
            }
        }
        Element genericInformationResistances = Optional.ofNullable(
                genericInformation.selectFirst("b:contains(Resistances)"))
                .map(container -> container.closest("tbody"))
                .orElseThrow(() -> new MissingSectionException("genericInformationResistances"));
        Element row = genericInformationResistances.child(1);

        parseResistances(row.text(), egoResistances, builder);
        // TODO Znormalizować zapis skillslotow
        scrapeEGOInformation(htmlContent, builder, hasNoCorrosion);
        scrapeEGOPortraits(htmlContent, builder, hasNoCorrosion);

        return true;
    }

    // TODO Pomyśl czy nie lepiej stworzyć nowego abilityBuildera zamiast robić to tak
    private void scrapeEGOInformation(Document htmlContent, DTOBuilders.EGODataBuilder builder,
                                      boolean hasNoCorrosion)
    {
        DTOBuilders.AbilityDataBuilder abilityBuilder = new DTOBuilders.AbilityDataBuilder();
        Element awakeningInformation;
        abilityBuilder.setSkillSlot("Skill_1");
        if(!hasNoCorrosion)
        {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-awakening div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("awakeningInformation"));
            builder.addAbility(processSingleAbility(awakeningInformation, abilityBuilder, true));

            abilityBuilder.setSkillSlot("Skill_2");
            Element corrosionInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("corrosionInformation"));
            builder.addAbility(processSingleAbility(corrosionInformation, abilityBuilder, true));
        }
        else
        {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:right]"))
                    .orElseThrow(() -> new MissingSectionException("awakeningInformation"));
            builder.addAbility(processSingleAbility(awakeningInformation, abilityBuilder, true));
        }
    }

    private static void scrapeEGOPortraits(Document htmlContent, DTOBuilders.EGODataBuilder builder,
                                           boolean hasNoCorrosion)
    {
        Element awakeningPortrait;
        if(!hasNoCorrosion)
        {
            awakeningPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-awakening div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("awakeningPortrait"));

            Element corrosionPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("corrosionPortrait"));

            builder.setCorrededPortraitFile(corrosionPortrait.attr("alt"));
            scrapeImageURL(corrosionPortrait);
        }
        else
        {
            awakeningPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElseThrow(() -> new MissingImageException("awakeningPortrait"));
        }
        builder.setPortraitFile(awakeningPortrait.attr("alt"));
    }

    private static void scrapeSanityCost(DTOBuilders.EGODataBuilder builder, Element dataRow, boolean isOverclocked)
    {
        int sanityValue = Integer.parseInt(dataRow.text());
        if(isOverclocked)
        {
            log.info("Corrosion sanity cost: {}", sanityValue);
            builder.setCorrosionSanityCost(sanityValue);
        }
        else
        {
            log.info("Awakening sanity cost: {}", sanityValue);
            builder.setAwakenSanityCost(sanityValue);
        }
    }

    private static void scrapeEGOResourceCost(DTOBuilders.EGODataBuilder builder, Element dataRow, boolean isOverclocked)
    {
        StringBuilder resourcesEGO = new StringBuilder();

        Map<String, Integer> useCost = new LinkedHashMap<>();
        String sinType = "";
        String sinCost;
        for(Node dataNode : dataRow.childNodes())
        {
            sinCost = "";
            if(dataNode instanceof Element nodeElement)
            {
                if(nodeElement.tagName().equals("img"))
                {
                    sinType = nodeElement.attr("alt");
                    resourcesEGO.append("[").append(sinType).append("]").append(" ");
                }
                else if(nodeElement.tagName().equals("span"))
                {
                    sinCost = nodeElement.text().replaceAll("\\D", "");
                }
            }
            else if(dataNode instanceof TextNode)
            {
                sinCost = ((TextNode) dataNode).text().replaceAll("\\D", "");
            }
            if(!sinCost.isEmpty())
            {
                resourcesEGO.append(sinCost).append(" ");
                useCost.put(sinType, Integer.parseInt(sinCost));
            }
        }
        if(isOverclocked)
        {
            log.info("Corrosion E.G.O Resources: {}", resourcesEGO.toString().trim());
            builder.setCorrosionSinCost(useCost);
        }
        else
        {
            log.info("Awakening E.G.O Resources: {}", resourcesEGO.toString().trim());
            builder.setAwakenSinCost(useCost);
        }
    }

    private static void scrapeEgoGenericInformation(DTOBuilders.EGODataBuilder builder, Element genericInformation, boolean hasNoCorrosion)
    {
        Elements genericRows;
        Element genericInformationInfo = Optional.ofNullable(genericInformation)
                    .map(info -> info.selectFirst("b:contains(Info)"))
                    .map(container -> container.closest("tbody"))
                    .orElseThrow(() -> new MissingSectionException("genericInformationInfo"));
        genericRows = genericInformationInfo.getElementsByTag("tr");

        for(Element row : genericRows)
        {
            Elements specifiedRow = row.select("td");
            if(row.text().trim().isEmpty())
            {
                continue;
            }
            if(specifiedRow.selectFirst("td:contains(Risk Level)") != null)
            {
                String riskLevel = extractAltFromSpecifiedIndex(specifiedRow, 1, "riskLevel");
                builder.setThreatLevel(ThreatLevel.threatLevelParser(riskLevel));
                log.info("Risk Level: {}", riskLevel);

                String season = hasNoCorrosion ? "Season 0" : extractTextFromSpecifiedIndex(specifiedRow,3, "season");
                builder.setSeason(season);
                log.info("Season: {}", season);
            }
            else if(specifiedRow.selectFirst("td:contains(Affinity)") != null) {

                String affinity = extractAltFromSpecifiedIndex(specifiedRow, 1, "affinity");
                builder.setSinAffinity(affinity);
                log.info("Affinity: {}", affinity);

                String release = hasNoCorrosion ? "Day 1" : extractTextFromSpecifiedIndex(specifiedRow, 3, "release");
                builder.setReleaseDate(release);
                log.info("Release: {}", release);

                if(hasNoCorrosion)
                {
                    break;
                }
            }
            else if(specifiedRow.selectFirst("td:contains(Abnormality)") != null)
            {
                String abnormality = extractTextFromSpecifiedIndex(specifiedRow, 1, "abnormality");
                builder.setAbnormality(abnormality);
                log.info("Abnormality: {}", abnormality);
            }
        }
    }

    private static void parseResistances(String text, String[] resistanceNames, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        String[] resistances = text.split(" ");
        int index = 0;
        System.out.print("Resistances: ");
        for (String resistance : resistances)
        {
            if (resistance.startsWith("["))
            {
                // Czyścimy tekst
                String cleanNumber = resistance.replaceAll("[^\\d.]", "");
                if (!cleanNumber.isEmpty() && index < resistanceNames.length)
                {
                    // Zamieniamy na double
                    double multiplier = Double.parseDouble(cleanNumber);
                    // Dodajemy do buildera korzystając z nazwy z tablicy
                    builder.addResistance(resistanceNames[index], multiplier);
                    System.out.print(resistance + " ");
                    index++;
                }
            }
        }
    }

    // TODO Rozbić na mniejsze metody, pamiętaj o SINGLE RESPONSIBILITY PRINCIPLE
    // TODO dodaj zbieranie status effectów
    private UUID processSingleAbility(Element abilityContainer, DTOBuilders.AbilityDataBuilder abilityBuilder)
    {
        return processSingleAbility(abilityContainer, abilityBuilder, false);
    }

    private UUID processSingleAbility(Element abilityContainer, DTOBuilders.AbilityDataBuilder abilityBuilder, boolean isEgo)
    {
        // Żebym wiedział, co się dzieje
        String skillSlot = abilityContainer.id();
        if(!isEgo)
        {
            abilityBuilder.setSkillSlot(skillSlot);
        }
        log.info("PROCESSING: {}", skillSlot);

        // Zwraca pusty pojemnik, jeżeli nie istnieje
        Elements columns = Optional.ofNullable(abilityContainer.select("table tr").first())
                .map(row -> row.select("td"))
                .orElseThrow(() -> new MissingSectionException("columns"));

        // Z racji, że wiemy, że zawsze będziemy mieć daną kolejność kolumn,
        // możemy działać po kolejności pojawienia się
        Element leftAbilityPanel = extractElementFromSpecifiedIndex(columns, 0, "leftAbilityPanel");

        scrapeSinAffinity(abilityBuilder, leftAbilityPanel);
        scrapeSkillIcon(abilityBuilder, leftAbilityPanel);
        scrapePower(abilityBuilder, leftAbilityPanel);
        scrapeDamageType(abilityBuilder, leftAbilityPanel);

        Element rightAbilityPanel = extractElementFromSpecifiedIndex(columns, 1, "rightAbilityPanel");

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
    private static void scrapeEffects(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
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

    private static void scrapeOffenseLevel(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
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

    private static void scrapeAttackWeight(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        Element attackWeightElement = rightAbilityPanel.selectFirst("font");
        if(attackWeightElement == null)
        {
            throw new MissingTextException("attackWeightElement");
        }
        String attackWeight = attackWeightElement.text().replace("Atk Weight ", "").trim();
        abilityBuilder.setAttackWeight(attackWeight);
        log.info("Attack weight: {}", attackWeight);
    }

    private static void scrapeAbilityName(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
    {
        Element abilityNameElement = rightAbilityPanel.selectFirst("div span");
        if(abilityNameElement == null)
        {
            throw new MissingTextException("abilityNameElement");
        }
        String abilityName = abilityNameElement.text();
        abilityBuilder.setAbilityName(abilityName);
        log.info("Ability name: {}", abilityName);
    }

    private static void scrapeCoinCount(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel)
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

    private static void scrapeDamageType(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        // w Jsoup '>' oznacza bezpośrednie dziecko
        Element damageTypeElement = leftAbilityPanel.selectFirst("> img");
        if(damageTypeElement == null)
        {
            throw new MissingImageException("damageTypeElement");
        }
        String damageType = damageTypeElement.attr("alt");
        abilityBuilder.setDamageType(damageType);
        log.info("Damage type: {}", damageType.replace(".png", ""));
    }

    private static void scrapePower(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        Elements abilityPowers = leftAbilityPanel.select("b");
        Element basePowerElement = extractElementFromSpecifiedIndex(abilityPowers, 0, "basePowerElement");
        abilityBuilder.setBasePower(Integer.parseInt(basePowerElement.text()));
        log.info("Base power: {}", basePowerElement.text());

        Element coinPowerElement = extractElementFromSpecifiedIndex(abilityPowers, 1, "coinPowerElement");
        abilityBuilder.setCoinPower(coinPowerElement.text());
        log.info("Coin power: {}", coinPowerElement.text());
    }

    private static void scrapeSkillIcon(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=Icon.png], img[alt$=Skill.png]");
        if(skillIcon == null)
        {
            skillIcon = leftAbilityPanel.selectFirst(whitelistSelector);
        }
        if(skillIcon == null)
        {
            throw new MissingImageException("skillIcon");
        }
        scrapeImageURL(skillIcon);
        String skillIconFile = skillIcon.attr("alt");
        skillIconFile = skillIconFile.replaceAll("[<>:\"/\\\\|?*]", "");
        abilityBuilder.setSkillIconFile(skillIconFile);
        log.info(skillIconFile.replace(".png", ""));
    }

    private static void scrapeSinAffinity(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        // W razie, że nic nie dostaniemy, lepsze coś niż null
        boolean affinityFound = false;
        for (Element image : leftAbilityPanel.select("img"))
        {
            String altText = image.attr("alt");
            if (iconWhiteList.contains(altText))
            {
                abilityBuilder.setSinAffinity(altText);
                log.info("Sin affinity: {}", altText.replace(".png", ""));
                affinityFound = true;
                break;
            }
        }
        if(!affinityFound)
        {
            throw new MissingTextException("sinAffinity");
        }
    }

    private static Element extractElementFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingSectionException(fieldName);
        }
        return columns.get(columnIndex);
    }

    private static String extractAltFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingImageException(fieldName);
        }
        Element img = columns.get(columnIndex).selectFirst("img");
        if(img == null)
        {
            throw new MissingImageException(fieldName);
        }
        return img.attr("alt");
    }

    private static String extractTextFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingSectionException(fieldName);
        }
        String text = columns.get(columnIndex).text().trim();
        if(text.isEmpty())
        {
            throw new MissingTextException(fieldName);
        }
        return text;
    }
}
