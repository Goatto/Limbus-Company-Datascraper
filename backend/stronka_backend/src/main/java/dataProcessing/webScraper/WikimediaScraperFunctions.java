package dataProcessing.webScraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Optional;

import static dataProcessing.webScraper.ImageScraper.scrapeImageURL;

public class WikimediaScraperFunctions
{

    // Nie korzystać z tego za często, później dorobię funkcję, która ręcznie pobierze takie obrazki jak threat level,
    // jak i generic ikonki

    public static void scrapeGeneralData(Document htmlContent)
    {
        Element smallSelector = htmlContent.selectFirst(".mw-page-title-main");
        String unitTitle = (smallSelector != null) ? smallSelector.text() : "No title";
        System.out.println(unitTitle);

        smallSelector = htmlContent.selectFirst(".mw-collapsible-content a img");
        scrapeImageURL(smallSelector);
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
                    System.out.println(unitTrait.text());
                }
            }
            // Zawsze resistances, będę musiał jakoś wyjąć tylko wartości z nawiasów klamrowych
            if (specifiedRowSize == 3 && !specifiedRow.getFirst().text().isEmpty())
            {
                // Tu jest git
                System.out.println("Resistances: ");
                System.out.println(specifiedRow.getFirst().text());
                System.out.println(specifiedRow.get(1).text());
                System.out.println(specifiedRow.get(2).text());
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
                    Element rarityIMG = specifiedRow.get(1).selectFirst("img");
                    System.out.println("Rarity : " + Optional.ofNullable(rarityIMG)
                            .map(img -> img.attr("alt"))
                            .orElse("Image not found"));

                    Element worldIMG = specifiedRow.get(3).selectFirst("img");
                    System.out.println("World : " + Optional.ofNullable(worldIMG)
                            .map(img -> img.attr("alt"))
                            .orElse("Image not found"));
                    scrapeImageURL(worldIMG);
                }
                if(rowType.equals("SeasonRelease"))
                {
                    System.out.println("Season : " + specifiedRow.get(1).text());
                    System.out.println("Release : " + specifiedRow.get(3).text());
                }
            }
            // Zawsze życie + speed + def. level
            if (specifiedRowSize == 6)
            {
                System.out.println("Health: " + specifiedRow.get(1).text());
                System.out.println("Speed: " + specifiedRow.get(3).text());
                System.out.println("Defense level: " + specifiedRow.get(5).text());
            }
        }
    }
    public static void scrapeSanityData(Document htmlContent)
    {
        Elements largeSelector = htmlContent.select("#Sanity-0 table tr td div");
        for (Element row : largeSelector)
        {
            Element sanityBox;
            // Muszę teraz jakoś przejść przez wszystkie linijki i je połączyć sensownie
            if (row.select("ul span").text().contains("increasing Sanity"))
            {
                System.out.println("Sanity+:");
                // Może jakiegoś splita mogę zrobić przed słowem 'Increase'
                sanityBox = row.child(1);
                Elements sanityEffects = sanityBox.getElementsByTag("li");
                for(Element sanityEffect : sanityEffects)
                {
                    System.out.println(sanityEffect.text());

                }
            }
            if (row.select("ul span").text().contains("decreasing Sanity"))
            {
                System.out.println("Sanity-:");
                sanityBox = row.child(1);
                Elements sanityEffects = sanityBox.getElementsByTag("li");
                for(Element sanityEffect : sanityEffects)
                {
                    System.out.println(sanityEffect.text());

                }
            }
        }
    }

    public static void scrapeIDAbilityData(Document htmlContent)
    {
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
    }
    public static void scrapePassiveData(Document htmlContent)
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
                Element costSin = divPassiveContainer.selectFirst("img[alt^=LcbSin]");
                Element costType = divPassiveContainer.selectFirst("span[style*=Mikodacs]");
                if(costType != null && costSin != null)
                {
                    System.out.println("Cost: " + costSin.attr("alt").replace(".png", "")
                            + " " + costType.text());
                    // Irytujący krzyżyk
                    Optional.ofNullable(costSin.nextElementSibling())
                            //TODO: Czary-mary, później doczytać jak to dokładnie działa
                            .ifPresentOrElse(Node::remove, () -> System.out.println("Cannot remove"));
                    costSin.remove();
                    costType.remove();
                }
                Element passiveTitle = divPassiveContainer.selectFirst("div[style*=fit-content]");
                if(passiveTitle != null)
                {
                    System.out.println("Passive title: " + passiveTitle.text().trim());
                    passiveTitle.remove();
                }
                String[] passiveContent = longTextSlasher(divPassiveContainer);
            }
        }
    }

    public static void scrapeEGOAbilities(Document htmlContent)
    {
        // Do wykorzystania i nadpisania przy przechodzeniu przez wiersze, żeby nie inicjalizować za każdym razem
        Elements genericRows;

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
                    System.out.println("Risk Level: " + Optional.ofNullable(
                            specifiedRow.get(1).selectFirst("img"))
                                .map(img -> img.attr("alt"))
                                .orElse("Image not found"));
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
                    else if(dataNode instanceof org.jsoup.nodes.TextNode)
                    {
                        String sinText = ((org.jsoup.nodes.TextNode) dataNode).text().trim();
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
            String rawResistances = row.text();
            String[] splitResistances = rawResistances.split(" ");
            System.out.print("Resistances: ");
            for(String word : splitResistances)
            {
                if(word.startsWith("["))
                {
                    System.out.print(word + " ");
                }
            }
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

    private static String[] longTextSlasher(Element divPassiveContainer)
    {
        divPassiveContainer.select("br").before("|||");
        String rawSplitPassive = divPassiveContainer.text();
        String[] splitPassive = rawSplitPassive.split("\\|\\|\\|");
        for (String passiveLine : splitPassive)
        {
            if (!passiveLine.trim().isEmpty())
            {
                System.out.println(passiveLine.trim());
            }
        }
        return splitPassive;
    }

    private static void processSingleAbility(Element abilityContainer)
    {
        List<String> iconWhiteList = List.of("Wrath1.png","Wrath2.png","Wrath3.png",
                "Lust1.png", "Lust2.png", "Lust3.png",
                "Sloth1.png", "Sloth2.png", "Sloth3.png",
                "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
                "Gloom1.png", "Gloom2.png", "Gloom3.png",
                "Pride1.png", "Pride2.png", "Pride3.png",
                "Envy1.png", "Envy2.png", "Envy3.png");

        // Żebym wiedział, co się dzieje
        System.out.println("\nPROCESSING: " + (abilityContainer.id()));

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

        for (Element image : leftAbilityPanel.select("img"))
        {
            String altText = image.attr("alt");
            if (iconWhiteList.contains(altText))
            {
                System.out.println("Sin affinity: " + altText.replace(".png", ""));
            }
        }
        Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=\" Icon.png\"]");
        scrapeImageURL(skillIcon);
        System.out.println(skillIcon != null ? "Skill icon: "
                + skillIcon.attr("alt").replace(".png", "") : "No skillIcon");

        Elements abilityPowers = leftAbilityPanel.select("b");
        Element basePower = abilityPowers.get(0);
        // w Jsoup '>' oznacza bezpośrednie dziecko
        Element damageType = leftAbilityPanel.selectFirst("> img");
        Element coinPower = abilityPowers.get(1);
        System.out.println("Base power: " + basePower.text());
        System.out.println(damageType != null ? "Damage type: "
                + damageType.attr("alt").replace(".png", "") : "No damageType");
        // Wyciąć z tego plusa
        System.out.println("Coin power: " + coinPower.text().replace("+ ", ""));

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
        System.out.println("Coin count: " + coinCount);

        Element abilityName = rightAbilityPanel.selectFirst("div span");
        System.out.println(abilityName != null ? "Ability name: "
                + abilityName.text() : "No abilityName");

        Element attackWeight = rightAbilityPanel.selectFirst("font");
        System.out.println(attackWeight != null ? "Attack weight: "
                + attackWeight.text().replace("Atk Weight ", "") : "No attackWeight");

        // ownText wyciąga tekst, który jest zawarty w pojemniku rightAbilityPanel,
        // ale nie w jego dzieciach
        String offenseLevel = rightAbilityPanel.textNodes().getFirst().text();
        System.out.println("Offense level: " + offenseLevel.replaceAll(".*([+-]\\d+).*", "$1"));

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
        String[] preAbilityContent =  longTextSlasher(preAbilityEffects);

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
                System.out.println("Coin" + coinNumber + " : " + effectText);
            }
        }
    }
}
