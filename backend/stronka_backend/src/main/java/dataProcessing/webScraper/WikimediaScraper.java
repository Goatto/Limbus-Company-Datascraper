package dataProcessing.webScraper;

import dataProcessing.DTOBuilders;
import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.AbilityService;
import dataProcessing.services.EGOService;
import dataProcessing.webScraper.enums.Tiers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

import static dataProcessing.webScraper.utils.ImageScraper.scrapeImageURL;

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

    static List<String> sinnerWhiteList = List.of("Yi Sang", "Faust", "Don Quixote", "Ryōshū",
            "Meursault", "Hong Lu", "Heathcliff", "Ishmael", "Rodion", "Sinclair", "Outis", "Gregor");

    static String[] egoResistances = {"Wrath", "Lust", "Sloth", "Gluttony", "Gloom", "Pride", "Envy"};

    private final AbilityService abilityService;
    private final EGOService EGOService;

    public WikimediaScraper(AbilityService abilityService, EGOService EGOService)
    {
        this.abilityService = abilityService;
        this.EGOService = EGOService;
    }

    // FIXME Nie radzi sobię z LCB Sinner ID
    public static DTOBuilders.IDDataBuilder scrapeGeneralIDData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        String[] idResistances = {"Slash", "Pierce", "Blunt"};
        scrapeName(htmlContent, builder);
        Element smallSelector;

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");

        // Pobieramy obrazek i od razu ustawiamy nazwę
        if(smallSelector == null)
        {
            return null;
        }
        builder.setPortraitFile(scrapeImageURL(smallSelector));
        Elements largeSelector = htmlContent.select("#General_Info-0 table tr");
        for (Element row : largeSelector)
        {
            // Używane do zdobycia określonego elementu, nie chcę mi się tego inicjalizować milion razy
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
        String unitTitle = (titleSelector != null) ? titleSelector.text() : "No title";
        // TODO Wykombinować jakiś lepszy sposób na to
        for(String sinner : sinnerWhiteList)
        {
            if(unitTitle.contains(sinner))
            {
                System.out.println("Sinner: " + sinner);
                builder.setSinnerName(sinner);
                break;
            }
        }
        builder.setName(unitTitle);
        System.out.println(unitTitle);
    }

    private static void scrapeStaggerThresholds(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        Element specificatorHelper;
        specificatorHelper = extractElementFromSpecifiedIndex(specifiedRow, 0);
        Elements staggerThresholds = specificatorHelper.getElementsByTag("font");
        System.out.println("Stagger Threshholds: ");
        for(Element staggerThreshold : staggerThresholds)
        {
            builder.addStaggerThreshold(staggerThreshold.text());
            System.out.println(staggerThreshold.text());
        }
    }

    private static void scrapeTraits(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        Element specificatorHelper;
        specificatorHelper = extractElementFromSpecifiedIndex(specifiedRow, 1);
        Elements unitTraits = specificatorHelper.getElementsByTag("b");
        System.out.println("Traits: ");
        for(Element unitTrait : unitTraits)
        {
            builder.addTrait(unitTrait.text());
            System.out.println(unitTrait.text());
        }
    }

    private static void scrapeTypeData(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        String rowType = "ERROR!";
        String rowText = extractTextFromSpecifiedIndex(specifiedRow, 0);
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
            String rarityIMG = extractAltFromSpecifiedIndex(specifiedRow, 1);
            builder.setRarity(Tiers.Rarity.rarityParser(rarityIMG));
            System.out.println("Rarity : " + rarityIMG);

            Element worldIMGURL = extractElementFromSpecifiedIndex(specifiedRow, 3).selectFirst("img");
            String worldIMG = extractAltFromSpecifiedIndex(specifiedRow, 3);
            builder.setWorldFile(worldIMG);
            builder.setWorld(worldIMG.replace("Icon.png", ""));
            System.out.println("World : " + worldIMG);
            scrapeImageURL(worldIMGURL);
        }
        else if(rowType.equals("SeasonRelease"))
        {
            String season = extractTextFromSpecifiedIndex(specifiedRow, 1);
            builder.setSeason(season);
            System.out.println("Season : " + season);

            String release = extractTextFromSpecifiedIndex(specifiedRow, 3);
            builder.setReleaseDate(release);
            System.out.println("Release : " + release);
        }
    }

    private static void scrapeBaseStats(DTOBuilders.IDDataBuilder builder, Elements specifiedRow) {
        int health = Integer.parseInt(extractTextFromSpecifiedIndex(specifiedRow, 1));
        builder.setHealth(health);
        System.out.println("Health: " + health);

        String speed = extractTextFromSpecifiedIndex(specifiedRow, 3);
        builder.setSpeed(speed);
        System.out.println("Speed: " + speed);

        int defenseLevel = Integer.parseInt(extractTextFromSpecifiedIndex(specifiedRow, 5));
        builder.setDefenseLevel(defenseLevel);
        System.out.println("Defense level: " + defenseLevel);
    }

    // TODO Stworzyć osobnę DTO dla sanity
    public static DTOBuilders.IDDataBuilder scrapeSanityData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        Elements sanitySelector = htmlContent.select("#Sanity-0 table tr td div");
        for (Element sanitySection : sanitySelector)
        {
            String headerText = sanitySection.select("ul span").text();
            Elements sanityEffects = sanitySection.select("ul li");
            if (headerText.contains("increasing Sanity"))
            {
                System.out.println("Sanity+:");
                for(Element sanityEffect : sanityEffects)
                {
                    builder.addPositiveSanityEffect(sanityEffect.text().trim());
                    System.out.println(sanityEffect.text());

                }
            }
            else if (headerText.contains("decreasing Sanity"))
            {
                System.out.println("Sanity-:");
                for(Element sanityEffect : sanityEffects)
                {
                    builder.addNegativeSanityEffect(sanityEffect.text().trim());
                    System.out.println(sanityEffect.text());

                }
            }
        }
        return builder;
    }

    // TODO jeżeli chcę dodać wstawianie umiejętności do db, musiałbym tutaj nad tym operować
    public List<ScraperDataDTOs.Ability> scrapeIDAbilityData(Document htmlContent)
    {
        List<ScraperDataDTOs.Ability> abilities = new ArrayList<>();

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
            // TODO tutaj chyba odbywało by się dodawanie
           processSingleAbility(element, new DTOBuilders.IDDataBuilder(), new DTOBuilders.AbilityDataBuilder());
        }
        return abilities;
    }

    // TODO Dodać dodawanie pasywek do DB
    public static void scrapePassiveData(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        // Ciekawy styl jest z tym b:contains...
        Elements prePassiveHeaders = htmlContent.select("b:contains(Passive), "
                + "b:contains(Combat Passives), b:contains(Support Passive), b:contains(Passives)");

        String passiveCategory = "";
        for (Element prePassiveHeader : prePassiveHeaders)
        {
            String previousPassiveCategory = passiveCategory;
            String headerText = prePassiveHeader.text().trim();
            if (headerText.contains("Combat Passive") || headerText.equals("Passives") || headerText.equals("Passive"))
            {
                passiveCategory = "COMBAT_PASSIVE";
            }
            // else-if bo inaczej możemy dostać false-positive dla continue
            else if (headerText.contains("Support Passive"))
            {
                passiveCategory = "SUPPORT_PASSIVE";
            }
            else
            {
                continue;
            }
            // Z racji struktury strony, mamy kilka razy kontenery o tej samej zawartości, z racji,
            // że nie chcę wypisywac ich wszystkich, robimy break, z jakiejś racji dochodzi do tego tylko dla E.G.O,
            // więc w poniższy warunek jest odpowiedni
            if(previousPassiveCategory.equals(passiveCategory))
            {
                break;
            }
            System.out.println(passiveCategory);
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
                            .orElse(new Element("div")));
            Elements divPassiveContainers = passiveContainer.select("div[style*=padding:10px]");

            for(Element divPassiveContainer : divPassiveContainers)
            {
                // TODO Dodać tu buildera pasywek
                ScraperDataDTOs.Passive passive = ScraperNodeVisitors.extractPassive(divPassiveContainer);

                switch(passiveCategory)
                {
                    // TODO To chyba mogę jakoś poprawić
                    case "COMBAT_PASSIVE" ->
                    {
                        if(builder instanceof DTOBuilders.IDDataBuilder idBuilder)
                        {
                            idBuilder.addCombatPassive(passive);

                        }
                        else if(builder instanceof DTOBuilders.EGODataBuilder egoBuilder)
                        {
                            egoBuilder.addCombatPassive(passive);
                        }
                    }
                    case "SUPPORT_PASSIVE" ->
                    {
                        if(builder instanceof DTOBuilders.IDDataBuilder idBuilder)
                        {
                            idBuilder.setSupportPassive(passive);
                        }
                    }
                }

            }
        }
    }

    // TODO Rozbić na mniejsze metody, pamiętaj o SINGLE RESPONSIBILITY PRINCIPLE
    public Object scrapeEGOAbilities(Document htmlContent, DTOBuilders.EGODataBuilder builder)
    {
        // FIXME Brak tytułu dla https://limbuscompany.wiki.gg/wiki/To_Remain_Oneself_%E5%AE%81%E4%BD%9C%E5%90%BE_Hong_Lu
        scrapeName(htmlContent, builder);

        Element genericInformation = Optional.of(htmlContent)
                .map(content -> content.selectFirst(".mw-body-content"))
                .orElse(new Element("div"));

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
                .orElse(new Elements());

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

            Element headerRow = extractElementFromSpecifiedIndex(specifiedRow, 0);
            Element dataRow = extractElementFromSpecifiedIndex(specifiedRow, 1);

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
                .orElse(new Element("div"));
        Element row = genericInformationResistances.child(1);

        parseResistances(row.text(), egoResistances, builder);
        // FIXME EGO nie otrzymują skillslota w builderze dla ability
        DTOBuilders.AbilityDataBuilder abilityBuilder = new DTOBuilders.AbilityDataBuilder();

        scrapeEGOInformation(htmlContent, builder, abilityBuilder, hasNoCorrosion);
        scrapeEGOPortraits(htmlContent, builder, hasNoCorrosion);

        EGOService.saveNewEGO(builder.buildEGOData());
        return true;
    }

    // TODO Pomyśl czy nie lepiej stworzyć nowego abilityBuildera zamiast robić to tak
    private void scrapeEGOInformation(Document htmlContent, DTOBuilders.EGODataBuilder builder,
                                      DTOBuilders.AbilityDataBuilder abilityBuilder, boolean hasNoCorrosion)
    {
        Element awakeningInformation;
        abilityBuilder.setSkillSlot("1");
        if(!hasNoCorrosion)
        {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-awakening div.ABMobile[style*=float:right]"))
                    .orElse(new Element("div"));
            builder.addAbility(processSingleAbility(awakeningInformation, builder, abilityBuilder));

            abilityBuilder.setSkillSlot("2");
            Element corrosionInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:right]"))
                    .orElse(new Element("div"));
            builder.addAbility(processSingleAbility(corrosionInformation, builder, abilityBuilder));
        }
        else
        {
            awakeningInformation = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:right]"))
                    .orElse(new Element("div"));
            builder.addAbility(processSingleAbility(awakeningInformation, builder, abilityBuilder));
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
                    .orElse(new Element("div"));

            Element corrosionPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            "#mw-customcollapsible-corrosion div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElse(new Element("div"));

            builder.setCorrededPortraitFile(corrosionPortrait.attr("alt"));
            scrapeImageURL(corrosionPortrait);
        }
        else
        {
            awakeningPortrait = Optional.of(htmlContent)
                    .map(content -> content.selectFirst(
                            ".mw-content-ltr div.ABMobile[style*=float:left] img[alt$=.png]"))
                    .orElse(new Element("div"));
        }
        builder.setPortraitFile(awakeningPortrait.attr("alt"));
    }

    private static void scrapeSanityCost(DTOBuilders.EGODataBuilder builder, Element dataRow, boolean isOverclocked)
    {
        int sanityValue = Integer.parseInt(dataRow.text());
        if(isOverclocked)
        {
            System.out.println("Corrosion sanity cost: " + sanityValue);
            builder.setCorrosionSanityCost(sanityValue);
        }
        else
        {
            System.out.println("Awakening sanity cost: " + sanityValue);
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
            System.out.println("Corrosion E.G.O Resources: " + resourcesEGO.toString().trim());
            builder.setCorrosionSinCost(useCost);
        }
        else
        {
            System.out.println("Awakening E.G.O Resources: " + resourcesEGO.toString().trim());
            builder.setAwakenSinCost(useCost);
        }
    }

    private static void scrapeEgoGenericInformation(DTOBuilders.EGODataBuilder builder, Element genericInformation, boolean hasNoCorrosion)
    {
        Elements genericRows;
        Element genericInformationInfo = Optional.ofNullable(
                genericInformation.selectFirst("b:contains(Info)"))
                    .map(container -> container.closest("tbody"))
                    .orElse(new Element("div"));
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
                if(hasNoCorrosion)
                {
                    String riskLevel = extractAltFromSpecifiedIndex(specifiedRow, 3);
                    builder.setThreatLevel(Tiers.ThreatLevel.threatLevelParser(riskLevel));
                    System.out.println("Risk Level: " + riskLevel);

                    String season = "Season 0";
                    builder.setSeason(season);
                    System.out.println("Season: " + season);

                    String affinity = extractAltFromSpecifiedIndex(specifiedRow, 1);
                    builder.setSinAffinity(affinity);
                    System.out.println("Affinity: " + affinity);

                    String release = "Day 1";
                    builder.setReleaseDate(release);
                    System.out.println("Release: " + release);

                    break;
                }
                String riskLevel = extractAltFromSpecifiedIndex(specifiedRow, 1);
                builder.setThreatLevel(Tiers.ThreatLevel.threatLevelParser(riskLevel));
                System.out.println("Risk Level: " + riskLevel);

                String season = extractTextFromSpecifiedIndex(specifiedRow,3);
                builder.setSeason(season);
                System.out.println("Season: " + season);
            }
            else if(specifiedRow.selectFirst("td:contains(Affinity)") != null)
            {
                String affinity = extractAltFromSpecifiedIndex(specifiedRow, 1);

                builder.setSinAffinity(affinity);
                System.out.println("Affinity: " + affinity);

                String releaseDate = extractTextFromSpecifiedIndex(specifiedRow,3);
                builder.setReleaseDate(releaseDate);
                System.out.println("Release: " + releaseDate);
            }
            else if(specifiedRow.selectFirst("td:contains(Abnormality)") != null)
            {
                String abnormality = extractTextFromSpecifiedIndex(specifiedRow, 1);
                builder.setAbnormality(abnormality);
                System.out.println("Abnormality: " + abnormality);
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
        System.out.println();
    }

    // TODO Rozbić na mniejsze metody, pamiętaj o SINGLE RESPONSIBILITY PRINCIPLE
    // TODO dodaj zbieranie status effectów
    private UUID processSingleAbility(Element abilityContainer, DTOBuilders.BaseEquippableBuilder<?> builder, DTOBuilders.AbilityDataBuilder abilityBuilder)
    {
        // Żebym wiedział, co się dzieje
        String skillSlot = abilityContainer.id();
        abilityBuilder.setSkillSlot(skillSlot);
        System.out.println("\nPROCESSING: " + skillSlot);

        // Zwraca pusty pojemnik, jeżeli nie istnieje
        Elements columns = Optional.ofNullable(abilityContainer.select("table tr").first())
                .map(row -> row.select("td"))
                .orElseGet(() -> {
                    System.out.println("No td for table tr");
                    return new Elements();
                });

        // Z racji, że wiemy, że zawsze będziemy mieć daną kolejność kolumn,
        // możemy działać po kolejności pojawienia się
        Element leftAbilityPanel = extractElementFromSpecifiedIndex(columns, 0);

        scrapeSinAffinity(abilityBuilder, leftAbilityPanel);
        scrapeSkillIcon(abilityBuilder, leftAbilityPanel);
        scrapePower(abilityBuilder, leftAbilityPanel);
        scrapeDamageType(abilityBuilder, leftAbilityPanel);

        Element rightAbilityPanel = extractElementFromSpecifiedIndex(columns, 1);

        scrapeCoinCount(abilityBuilder, rightAbilityPanel);
        scrapeAbilityName(abilityBuilder, rightAbilityPanel);
        scrapeAttackWeight(abilityBuilder, rightAbilityPanel);
        scrapeOffenseLevel(abilityBuilder, rightAbilityPanel);
        scrapeEffects(abilityBuilder, rightAbilityPanel);

        // TODO Powinienem tutaj bezpośrednio budować umiejętnośc do DB, a do DTO zwykle dodać jej klucz główny
        ScraperDataDTOs.Ability builtAbility = abilityBuilder.buildAbilityData();
        return abilityService.saveNewAbility(builtAbility);
    }

    // FIXME Nie ma base effects dla https://limbuscompany.wiki.gg/wiki/9:2_Sinclair
    private static void scrapeEffects(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel) {
        ScraperNodeVisitors.AbilityResult result = ScraperNodeVisitors.extractAbility(rightAbilityPanel);

        abilityBuilder.setBaseEffects(result.baseEffects());
        System.out.println("Base effects:");
        if (result.baseEffects().isEmpty())
        {
            System.out.println("- No Base Effects");
        }
        else
        {
            result.baseEffects().forEach(effect -> System.out.println("- " + effect));
        }

        abilityBuilder.setCoinEffect(result.coinEffects());
        result.coinEffects().forEach((coinNum, effects) ->
        {
            System.out.println("Coin " + coinNum + " effects: ");
            effects.forEach(effect -> System.out.println("- " + effect));
        });
    }

    private static void scrapeOffenseLevel(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel) {
        String offenseLevel = rightAbilityPanel.textNodes().stream()
                        .map(tn -> tn.text().trim())
                        .filter(t -> t.matches(".*[+-]\\d+.*"))
                        .findFirst()
                        .map(t -> t.replaceAll(".*([+-]\\d+).*", "$1"))
                        .orElse("0");
        abilityBuilder.setOffenseLevel(offenseLevel);
        System.out.println("Offense level: " + offenseLevel);
    }

    private static void scrapeAttackWeight(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel) {
        Element attackWeightElement = rightAbilityPanel.selectFirst("font");
        String attackWeight = attackWeightElement != null ?
                attackWeightElement.text().replace("Atk Weight ", "") : "NO ATTACK WEIGHT";
        abilityBuilder.setAttackWeight(attackWeight);
        System.out.println("Attack weight: "  + attackWeight);
    }

    private static void scrapeAbilityName(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel) {
        Element abilityNameElement = rightAbilityPanel.selectFirst("div span");
        String abilityName = abilityNameElement != null ? abilityNameElement.text() : "NO ABILITY NAME";
        abilityBuilder.setAbilityName(abilityName);
        System.out.println("Ability name: " + abilityName);
    }

    private static void scrapeCoinCount(DTOBuilders.AbilityDataBuilder abilityBuilder, Element rightAbilityPanel) {
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
        System.out.println("Coin count: " + coinCount);
    }

    private static void scrapeDamageType(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel) {
        // w Jsoup '>' oznacza bezpośrednie dziecko
        Element damageTypeElement = leftAbilityPanel.selectFirst("> img");
        String damageType = damageTypeElement != null ? damageTypeElement.attr("alt") : "NO DAMAGE TYPE ICON FOUND";
        abilityBuilder.setDamageType(damageType);
        System.out.println("Damage type: " + damageType.replace(".png", ""));
    }

    private static void scrapePower(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel) {
        Elements abilityPowers = leftAbilityPanel.select("b");
        Element basePowerElement = extractElementFromSpecifiedIndex(abilityPowers, 0);
        abilityBuilder.setBasePower(Integer.parseInt(basePowerElement.text()));
        System.out.println("Base power: " + basePowerElement.text());

        Element coinPowerElement = extractElementFromSpecifiedIndex(abilityPowers, 1);
        abilityBuilder.setCoinPower(coinPowerElement.text());
        System.out.println("Coin power: " + coinPowerElement.text());
    }

    private static void scrapeSkillIcon(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel) {
        Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=Icon.png], img[alt$=Skill.png]");
        scrapeImageURL(skillIcon);
        String skillIconFile = skillIcon != null ? skillIcon.attr("alt") : "NO SKILL ICON FOUND";
        abilityBuilder.setSkillIconFile(skillIconFile);
        System.out.println(skillIconFile.replace(".png", ""));
    }

    private static void scrapeSinAffinity(DTOBuilders.AbilityDataBuilder abilityBuilder, Element leftAbilityPanel)
    {
        // W razie, że nic nie dostaniemy, lepsze coś niż null
        abilityBuilder.setSinAffinity("NO SIN AFFINITY FOUND");
        for (Element image : leftAbilityPanel.select("img"))
        {
            String altText = image.attr("alt");
            if (iconWhiteList.contains(altText))
            {
                abilityBuilder.setSinAffinity(altText);
                System.out.println("Sin affinity: " + altText.replace(".png", ""));
            }
        }
    }

    private static Element extractElementFromSpecifiedIndex(Elements columns, int columnIndex)
    {
        if(columnIndex >= columns.size())
        {
            return new Element("div");
        }
        return columns.get(columnIndex);
    }

    private static String extractAltFromSpecifiedIndex(Elements columns, int columnIndex)
    {
        if(columnIndex >= columns.size())
        {
            return "NO IMAGE!";
        }
        Element img = columns.get(columnIndex).selectFirst("img");
        return img != null ? img.attr("alt") : "NO IMAGE!";
    }

    private static String extractTextFromSpecifiedIndex(Elements columns, int columnIndex)
    {
        if(columnIndex >= columns.size())
        {
            return "NO TEXT!";
        }
        String text = columns.get(columnIndex).text().trim();
        return !text.isEmpty() ? text : "NO TEXT!";
    }
}
