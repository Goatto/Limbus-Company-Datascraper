package dataProcessing.webScraper;

import dataProcessing.DTOBuilders;
import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.AbilityService;
import dataProcessing.webScraper.enums.Tiers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dataProcessing.webScraper.utils.ImageScraper.scrapeImageURL;

@Component
public class WikimediaScraper
{
    private final AbilityService abilityService;

    public WikimediaScraper(AbilityService abilityService)
    {
        this.abilityService = abilityService;
    }

    // TODO pozbyć się wszystkich instancji remove

    public static DTOBuilders.IDDataBuilder scrapeGeneralIDData(Document htmlContent, DTOBuilders.IDDataBuilder builder)
    {
        String[] idResistances = {"Slash", "Pierce", "Blunt"};

        Element smallSelector = htmlContent.selectFirst(".mw-page-title-main");
        String unitTitle = (smallSelector != null) ? smallSelector.text() : "No title";
        builder.setName(unitTitle);
        System.out.println(unitTitle);

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");

        // Pobieramy obrazek i od razu ustawiamy nazwę
        builder.setPortraitFile(scrapeImageURL(smallSelector));
        Elements largeSelector = htmlContent.select("#General_Info-0 table tr");
        for (Element row : largeSelector)
        {
            // Używane do zdobycia określonego elementu, nie chcę mi się tego inicjalizować milion razy
            Element specificatorHelper;
            Elements specifiedRow = row.select("td");
            int specifiedRowSize = specifiedRow.size();
            // Zawsze treshholdy staggera
            if (specifiedRowSize == 1)
            {
                specificatorHelper = specifiedRow.getFirst();
                Elements staggerThresholds = specificatorHelper.getElementsByTag("font");
                System.out.println("Stagger Threshholds: ");
                for(Element staggerThreshold : staggerThresholds)
                {
                    builder.addStaggerThreshold(staggerThreshold.text());
                    System.out.println(staggerThreshold.text());
                }
            }
            // Zawsze tylko traits
            // Wykombinować jak to rozdzielić, pewnie będzie to jakaś lista stringów w klasie wsmie
            else if (specifiedRowSize == 2)
            {
                specificatorHelper = specifiedRow.get(1);
                Elements unitTraits = specificatorHelper.getElementsByTag("b");
                System.out.println("Traits: ");
                for(Element unitTrait : unitTraits)
                {
                    builder.addTrait(unitTrait.text());
                    System.out.println(unitTrait.text());
                }
            }
            // Zawsze resistances, będę musiał jakoś wyjąć tylko wartości z nawiasów klamrowych
            else if (specifiedRowSize == 3 && !specifiedRow.getFirst().text().isEmpty())
            {
                // Tu jest git
                parseResistances(specifiedRow.text(), idResistances, builder);
            }

           else if (specifiedRowSize == 4)
            {
                String rowType = "ERROR!";
                if(specifiedRow.get(0).text().contains("Rarity"))
                {
                    rowType = "RarityWorld";
                }
               else if(specifiedRow.get(0).text().contains("Season"))
                {
                    rowType = "SeasonRelease";
                }
                if(rowType.equals("RarityWorld"))
                {
                    String rarityIMG = Optional.ofNullable(specifiedRow.get(1).selectFirst("img"))
                            .map(img -> img.attr("alt"))
                            .orElse("IMAGE NOT FOUND");
                    builder.setRarity(Tiers.Rarity.rarityParser(rarityIMG));
                    System.out.println("Rarity : " + rarityIMG);

                    Element worldIMGURL = specifiedRow.get(3).selectFirst("img");
                    String worldIMG = Optional.ofNullable(worldIMGURL)
                            .map(img -> img.attr("alt"))
                            .orElse("IMAGE NOT FOUND");
                    builder.setWorldFile(worldIMG);
                    builder.setWorld(worldIMG.replace("Icon.png", ""));
                    System.out.println("World : " + worldIMG);
                    scrapeImageURL(worldIMGURL);
                }
                else if(rowType.equals("SeasonRelease"))
                {
                    builder.setSeason(specifiedRow.get(1).text());
                    System.out.println("Season : " + specifiedRow.get(1).text());
                    builder.setReleaseDate(specifiedRow.get(3).text());
                    System.out.println("Release : " + specifiedRow.get(3).text());
                }
            }
            // Zawsze życie + speed + def. level
            else if (specifiedRowSize == 6)
            {
                builder.setHealth(Integer.parseInt(specifiedRow.get(1).text().trim()));
                System.out.println("Health: " + specifiedRow.get(1).text());
                builder.setSpeed(specifiedRow.get(3).text());
                System.out.println("Speed: " + specifiedRow.get(3).text());
                builder.setDefenseLevel(Integer.parseInt(specifiedRow.get(5).text().trim()));
                System.out.println("Defense level: " + specifiedRow.get(5).text());
            }
        }
        return builder;
    }

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
        //FIXME Nie zbiera 3-2 z jakiejś racji
        List<ScraperDataDTOs.Ability> abilities = new ArrayList<>();

        Elements largeSelector = htmlContent.select(".tabber .tabber__section [id^=Skill_1-], "
                + ".tabber .tabber__section [id^=Skill_2-], "
                + ".tabber .tabber__section [id^=Skill_3-], "
                + ".tabber .tabber__section [id^=Defense-], "
                + ".tabber .tabber__section [id^=Defense_]");
        for (Element element : largeSelector)
        {
            // Elementy -label nas nie interesują, jak i nie chcemy kontynerów
            if (element.id().endsWith("-label") || !element.select(".tabber").isEmpty())
            {
                continue;
            }
            // TODO tutaj chyba odbywało by się dodawanie
           abilityService.saveNewAbility(processSingleAbility(element));
        }
        return abilities;
    }
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
            {;
                ScraperDataDTOs.Passive passive = ScraperNodeVisitors.extractPassive(divPassiveContainer);
                switch(passiveCategory)
                {
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

    // TODO Wypełnić builderao
    // TODO to NAPEWNO rozbić na mniejsze funkcje, absolutnie mało czytelne i chaotycznie napisane
    public static void scrapeEGOAbilities(Document htmlContent, DTOBuilders.EGODataBuilder builder)
    {
        // Do wykorzystania i nadpisania przy przechodzeniu przez wiersze, żeby nie inicjalizować za każdym razem
        Elements genericRows;
        String[] egoResistances = {"Wrath", "Lust", "Sloth", "Gluttony", "Gloom", "Pride", "Envy"};
        Element genericInformation = Optional.ofNullable(htmlContent)
                .map(content -> content.selectFirst(".mw-body-content"))
                .orElse(new Element("div"));

        Element genericInformationInfo = Optional.ofNullable(
                genericInformation.selectFirst("b:contains(Info)"))
                    .map(container -> container.closest("tbody"))
                    .orElse(new Element("div"));
        // Jak mamy cały kontener, to usuwamy pierwsze dziecko, z napisem "Info"
        // Po czym przechodzimy po pozostałych kontenerach
        genericInformationInfo.child(0).remove();
        genericRows = genericInformationInfo.getElementsByTag("tr");
        for(Element row : genericRows)
        {
            Elements specifiedRow = row.select("td");
            if (!row.text().isEmpty())
            {
                if(specifiedRow.selectFirst("td:contains(Risk Level)") != null)
                {
                    String riskLevel = Optional.ofNullable(
                                    specifiedRow.get(1).selectFirst("img"))
                            .map(img -> img.attr("alt"))
                            .orElse("Image not found");
                    builder.setThreatLevel(Tiers.ThreatLevel.threatLevelParser(riskLevel));
                    System.out.println("Risk Level: " + riskLevel);
                    System.out.println("Season: " + specifiedRow.get(3).text());
                }
                else if(specifiedRow.selectFirst("td:contains(Affinity)") != null)
                {
                    System.out.println("Affinity: " + Optional.ofNullable(
                            specifiedRow.get(1).selectFirst("img"))
                                .map(img -> img.attr("alt"))
                                .orElse("Image not found"));
                    System.out.println("Release: " + specifiedRow.get(3).text());
                }
                else if(specifiedRow.selectFirst("td:contains(Abnormality)") != null)
                {
                    System.out.println("Abnormality: " + specifiedRow.get(1).text());
                }
            }
        }
        Element genericInformationCost = Optional.ofNullable(
                genericInformation.selectFirst("b:contains(Cost (Overclock))"))
                    .map(container -> container.closest("tbody"))
                    .orElse(new Element("div"));

        // Usuwamy headery
        // FIXME Wyrzuca się na https://limbuscompany.wiki.gg/wiki/Bodysack_Heathcliff
        genericInformationCost.child(0).remove();
        genericInformationCost.child(2).remove();
        genericRows = genericInformationCost.getElementsByTag("tr");
        for(Element row : genericRows)
        {
            Elements specifiedRow = row.select("td");

            if(specifiedRow.size() < 2)
            {
                continue;
            }
            Element headerRow = specifiedRow.get(0);
            Element dataRow = specifiedRow.get(1);
            if(headerRow.text().contains("Sanity"))
            {
                System.out.println("Sanity cost: " + dataRow.text());
            }
            else if(headerRow.text().contains("E.G.O Resources"))
            {
                System.out.println("E.G.O Resources: ");

                for(Node dataNode : dataRow.childNodes())
                {
                    if(dataNode instanceof Element nodeElement)
                    {
                        if(nodeElement.tagName().equals("img"))
                        {
                            String sinType = nodeElement.attr("alt").replace(".png", "");
                            System.out.print("[" + sinType + "]");
                        }
                        else if(nodeElement.tagName().equals("span"))
                        {
                            System.out.print(nodeElement.text().trim() + " ");
                        }
                    }
                    else if(dataNode instanceof TextNode)
                    {
                        String sinText = ((TextNode) dataNode).text().trim();
                        if(!sinText.isEmpty())
                        {
                            System.out.print(sinText + " ");
                        }
                    }
                }
                System.out.println();
            }
        }
        Element genericInformationResistances = Optional.ofNullable(
                genericInformation.selectFirst("b:contains(Resistances)"))
                .map(container -> container.closest("tbody"))
                .orElse(new Element("div"));
        genericInformationResistances.child(0).remove();
        genericRows = genericInformationResistances.getElementsByTag("tr");

        for(Element row : genericRows)
        {
            parseResistances(row.text(), egoResistances, new DTOBuilders.EGODataBuilder());
        }
        Element awakeningInformation = Optional.ofNullable(htmlContent)
                .map(content -> content.selectFirst(
                        "#mw-customcollapsible-awakening div.ABMobile[style*=float:right]"))
                .orElse(new Element("div"));
        processSingleAbility(awakeningInformation);

        Element corrosionInformation = Optional.ofNullable(htmlContent)
                .map(content -> content.selectFirst(
                        "#mw-customcollapsible-corrosion div.ABMobile[style*=float:right]"))
                .orElse(new Element("div"));
        processSingleAbility(corrosionInformation);
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

    // TODO też mógłbym chyba teoretycznie rozbić to na mniejsze funkcje, później o tym pomyśl
    // TODO dodaj zbieranie status effectów
    private static ScraperDataDTOs.Ability processSingleAbility(Element abilityContainer)
    {
        DTOBuilders.AbilityDataBuilder builder = new DTOBuilders.AbilityDataBuilder();

        List<String> iconWhiteList = List.of("Wrath1.png","Wrath2.png","Wrath3.png",
                "Lust1.png", "Lust2.png", "Lust3.png",
                "Sloth1.png", "Sloth2.png", "Sloth3.png",
                "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
                "Gloom1.png", "Gloom2.png", "Gloom3.png",
                "Pride1.png", "Pride2.png", "Pride3.png",
                "Envy1.png", "Envy2.png", "Envy3.png");

        // Żebym wiedział, co się dzieje
        String skillSlot = abilityContainer.id();
        builder.setSkillSlot(skillSlot);
        System.out.println("\nPROCESSING: " + skillSlot);

        // Zwraca pusty pojemnik, jeżeli nie istnieje
        Elements columns = Optional.ofNullable(abilityContainer.select("table tr").first())
                .map(row -> row.select("td"))
                .orElseGet(() -> {
                    System.out.println("No td for table tr");
                    return new Elements();
                });

        // Z racji, że wiemy, że zawsze będziemy mieć daną kolejnośc kolumn,
        // możemy działać po kolejności pojawienia się
        Element leftAbilityPanel = columns.getFirst();

        // W razie że nic nie dostaniemy, lepsze coś niż null
        builder.setSinAffinity("NO SIN AFFINITY FOUND");
        for (Element image : leftAbilityPanel.select("img"))
        {
            String altText = image.attr("alt");
            if (iconWhiteList.contains(altText))
            {
                builder.setSinAffinity(altText);
                System.out.println("Sin affinity: " + altText.replace(".png", ""));
            }
        }
        Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=\" Icon.png\"]");
        scrapeImageURL(skillIcon);
        String skillIconFile = skillIcon != null ? skillIcon.attr("alt") : "NO SKILL ICON FOUND";
        builder.setSkillIconFile(skillIconFile);
        System.out.println(skillIconFile.replace(".png", ""));

        Elements abilityPowers = leftAbilityPanel.select("b");
        Element basePowerElement = abilityPowers.get(0);
        // w Jsoup '>' oznacza bezpośrednie dziecko
        Element damageTypeElement = leftAbilityPanel.selectFirst("> img");
        Element coinPowerElement = abilityPowers.get(1);

        String damageType = damageTypeElement != null ? damageTypeElement.attr("alt") : "NO DAMAGE TYPE ICON FOUND";
        builder.setBasePower(Integer.parseInt(basePowerElement.text()));
        System.out.println("Base power: " + basePowerElement.text());
        builder.setDamageType(damageType);
        System.out.println("Damage type: " + damageType.replace(".png", ""));
        builder.setCoinPower(coinPowerElement.text());
        System.out.println("Coin power: " + coinPowerElement.text());

        Element rightAbilityPanel = columns.get(1);
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
        builder.setCoinCount(coinCount);
        System.out.println("Coin count: " + coinCount);

        Element abilityNameElement = rightAbilityPanel.selectFirst("div span");
        String abilityName = abilityNameElement != null ? abilityNameElement.text() : "NO ABILITY NAME";
        builder.setAbilityName(abilityName);
        System.out.println("Ability name: " + abilityName);

        Element attackWeightElement = rightAbilityPanel.selectFirst("font");
        String attackWeight = attackWeightElement != null ?
                attackWeightElement.text().replace("Atk Weight ", "") : "NO ATTACK WEIGHT";
        builder.setAttackWeight(attackWeight);
        System.out.println("Attack weight: "  + attackWeight);

        // ownText wyciąga tekst, który jest zawarty w pojemniku rightAbilityPanel,
        // ale nie w jego dzieciach
        // KATASTROFALNA kombinacja metody po metodzie
        // String offenseLevel = rightAbilityPanel.textNodes().getFirst().text().replaceAll(".*([+-]\\d+).*", "$1");

        // Powinno być bezpieczniejsze, ale nie wiem czy nie znacznie wolniejsze
        String offenseLevel = rightAbilityPanel.textNodes().stream()
                        .map(tn -> tn.text().trim())
                        .filter(t -> t.matches(".*[+-]\\d+.*"))
                        .findFirst()
                        .map(t -> t.replaceAll(".*([+-]\\d+).*", "$1"))
                        .orElse("0");

        builder.setOffenseLevel(offenseLevel);
        System.out.println("Offense level: " + offenseLevel);

        ScraperNodeVisitors.AbilityResult result = ScraperNodeVisitors.extractAbility(rightAbilityPanel);

        builder.setBaseEffects(result.baseEffects());
        System.out.println("Base effects:");
        if (result.baseEffects().isEmpty())
        {
            System.out.println("- No Base Effects");
        }
        else
        {
            result.baseEffects().forEach(effect -> System.out.println("- " + effect));
        }

        builder.setCoinEffect(result.coinEffects());
        result.coinEffects().forEach((coinNum, effects) ->
        {
            System.out.println("Coin " + coinNum + " effects: ");
            effects.forEach(effect -> System.out.println("- " + effect));
        });

        return builder.buildAbilityData();
    }
}
