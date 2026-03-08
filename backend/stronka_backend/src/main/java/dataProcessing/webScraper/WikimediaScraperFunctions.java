package dataProcessing.webScraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.*;

import static dataProcessing.webScraper.ImageScraper.scrapeImageURL;

public class WikimediaScraperFunctions
{

    // Nie korzystać z tego za często, później dorobię funkcję, która ręcznie pobierze takie obrazki jak threat level,
    // jak i generic ikonki

    public static RecordBuilders.IDDataBuilder scrapeGeneralIDData(Document htmlContent, RecordBuilders.IDDataBuilder builder)
    {
        String[] idResistances = {"Slash", "Pierce", "Blunt"};

        Element smallSelector = htmlContent.selectFirst(".mw-page-title-main");
        String unitTitle = (smallSelector != null) ? smallSelector.text() : "No title";
        builder.setName(unitTitle);
        System.out.println(unitTitle);

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");

        // Pobieramy obrazek i od razu ustawiamy nazwe
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
            if (specifiedRowSize == 2)
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
            if (specifiedRowSize == 3 && !specifiedRow.getFirst().text().isEmpty())
            {
                // Tu jest git
                parseResistances(specifiedRow.text(), idResistances, builder);
            }

            if (specifiedRowSize == 4)
            {
                String rowType = "ERROR!";
                if(specifiedRow.get(0).text().contains("Rarity"))
                {
                    rowType = "RarityWorld";
                }
                if(specifiedRow.get(0).text().contains("Season"))
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
                if(rowType.equals("SeasonRelease"))
                {
                    builder.setSeason(specifiedRow.get(1).text());
                    System.out.println("Season : " + specifiedRow.get(1).text());
                    builder.setReleaseDate(specifiedRow.get(3).text());
                    System.out.println("Release : " + specifiedRow.get(3).text());
                }
            }
            // Zawsze życie + speed + def. level
            if (specifiedRowSize == 6)
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

    public static RecordBuilders.IDDataBuilder scrapeSanityData(Document htmlContent, RecordBuilders.IDDataBuilder builder)
    {
        Elements largeSelector = htmlContent.select("#Sanity-0 table tr td div");
        for (Element row : largeSelector)
        {
            Element sanityBox = row.child(1);
            Elements sanityEffects = sanityBox.getElementsByTag("li");
            // Muszę teraz jakoś przejść przez wszystkie linijki i je połączyć sensownie
            if (row.select("ul span").text().contains("increasing Sanity"))
            {
                System.out.println("Sanity+:");
                for(Element sanityEffect : sanityEffects)
                {
                    builder.addPositiveSanityEffect(sanityEffect.text().trim());
                    System.out.println(sanityEffect.text());

                }
            }
            else if (row.select("ul span").text().contains("decreasing Sanity"))
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

    public static List<FormatedScraperData.Ability> scrapeIDAbilityData(Document htmlContent)
    {
        List<FormatedScraperData.Ability> abilities = new ArrayList<>();

        Elements largeSelector = htmlContent.select(".tabber .tabber__section [id^=Skill_1-], "
                + ".tabber .tabber__section [id^=Skill_2-], "
                + ".tabber .tabber__section [id^=Skill_3-], "
                + ".tabber .tabber__section [id^=Defense-], "
                + ".tabber .tabber__section [id^=Defense_]");
        for (Element element : largeSelector) {
            // Elementy -label nas nie interesują, jak i nie chcemy kontynerów
            if (element.id().endsWith("-label") || !element.select(".tabber").isEmpty())
            {
                continue;
            }
            processSingleAbility(element);
        }
        return abilities;
    }
    public static void scrapePassiveData(Document htmlContent, RecordBuilders.BaseEquippableBuilder<?> builder)
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
                // TODO napisać tę funkcję od nowa
//                Element costSin = divPassiveContainer.selectFirst("img[alt^=LcbSin]");
//                Element costType = divPassiveContainer.selectFirst("span[style*=Mikodacs]");
//                if(costType != null && costSin != null)
//                {
//                    System.out.println("Cost: " + costSin.attr("alt").replace(".png", "")
//                            + " " + costType.text());
//                    // Irytujący krzyżyk
//                    Optional.ofNullable(costSin.nextElementSibling())
//                            //TODO: Czary-mary, później doczytać jak to dokładnie działa
//                            .ifPresentOrElse(Node::remove, () -> System.out.println("Cannot remove"));
//                    costSin.remove();
//                    costType.remove();
//                }
//                Element passiveTitle = divPassiveContainer.selectFirst("div[style*=fit-content]");
//                if(passiveTitle != null)
//                {
//                    System.out.println("Passive title: " + passiveTitle.text().trim());
//                    passiveTitle.remove();
//                }
//                List<String> passiveContent = longTextSlasher(divPassiveContainer);
            }
        }
    }

    // TODO Wypełnić buildera
    public static void scrapeEGOAbilities(Document htmlContent, RecordBuilders.EGODataBuilder builder)
    {
        // Do wykorzystania i nadpisania przy przechodzeniu przez wiersze, żeby nie inicjalizować za każdym razem
        Elements genericRows;
        String[] egoResistances = {"Wrath", "Lust", "Sloth", "Gluttony", "Gloom", "Pride", "Envy"};
        Element genericInformation = Optional.ofNullable(htmlContent)
                .map(content -> content.selectFirst(".mw-body-content"))
                .orElse(new Element("div"));

        // TODO Chyba mogę zmodyfikować scrapeGeneralData by wykorzystywało informacje z tego miejsca
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
                if(specifiedRow.selectFirst("td:contains(Affinity)") != null)
                {
                    System.out.println("Affinity: " + Optional.ofNullable(
                            specifiedRow.get(1).selectFirst("img"))
                                .map(img -> img.attr("alt"))
                                .orElse("Image not found"));
                    System.out.println("Release: " + specifiedRow.get(3).text());
                }
                if(specifiedRow.selectFirst("td:contains(Abnormality)") != null)
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
            parseResistances(row.text(), egoResistances, new RecordBuilders.EGODataBuilder());
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

    private static List<String> longTextSlasher(Element divPassiveContainer)
    {
        divPassiveContainer.select("br").before("|||");
        String rawSplitPassive = divPassiveContainer.text();
        List<String> splitPassive = Arrays.asList(rawSplitPassive.split("\\|\\|\\|"));
        for (String passiveLine : splitPassive)
        {
            if (!passiveLine.trim().isEmpty())
            {
                System.out.println(passiveLine.trim());
            }
        }
        return splitPassive;
    }

    private static void parseResistances(String text, String[] resistanceNames, RecordBuilders.BaseEquippableBuilder<?> builder)
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

    private static FormatedScraperData.Ability processSingleAbility(Element abilityContainer)
    {
        RecordBuilders.AbilityDataBuilder builder = new RecordBuilders.AbilityDataBuilder();

        List<String> iconWhiteList = List.of("Wrath1.png","Wrath2.png","Wrath3.png",
                "Lust1.png", "Lust2.png", "Lust3.png",
                "Sloth1.png", "Sloth2.png", "Sloth3.png",
                "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
                "Gloom1.png", "Gloom2.png", "Gloom3.png",
                "Pride1.png", "Pride2.png", "Pride3.png",
                "Envy1.png", "Envy2.png", "Envy3.png");

        // Żebym wiedział, co się dzieje
        String skillSlot = abilityContainer.id();
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
            } else {
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
        String offenseLevel = rightAbilityPanel.textNodes().getFirst().text().replaceAll(".*([+-]\\d+).*", "$1");
        builder.setOffenseLevel(offenseLevel);
        System.out.println("Offense level: " + offenseLevel);

        Element preAbilityEffects = rightAbilityPanel.clone();
        // Zawsze powinno istnieć, ale chcemy ominąc wyrzucenie całego programu
        Optional.ofNullable(preAbilityEffects.selectFirst("div span")).ifPresentOrElse(
                Element::remove,
                () -> System.out.println("No 'div span' for removal"));
        Optional.ofNullable(preAbilityEffects.selectFirst("font")).ifPresentOrElse(
                Element::remove,
                () -> System.out.println("No 'font' for removal"));
        // Usuwamy elementy, żeby się nie powtarzały
        preAbilityEffects.textNodes().getFirst().remove();
        preAbilityEffects.select("div[style*=\"padding-left\"]").remove();
        // Będę musiał to jakoś trochę inaczej zrobić chyba
        builder.setBaseEffects(longTextSlasher(preAbilityEffects));

        Elements abilityCoins = rightAbilityPanel.select("div[style*=\"padding-left\"]");
        for (Element coin : abilityCoins)
        {
            // Szukamy obrazka o danym alt
            Element coinImage = coin.selectFirst("img[alt^=\"CoinEffect\"]");
            if (coinImage != null) {
                String coinNumber = coinImage.attr("alt");
                // Usuwamy wszystko, co nie jest liczbą
                coinNumber = coinNumber.replaceAll("\\D+", "");
                String effectText = coin.text();
                builder.addCoinEffect(coinNumber, effectText);
                System.out.println("Coin" + coinNumber + " : " + effectText);
            }

        }
        return builder.buildAbilityData();
    }
}
