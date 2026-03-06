package dataProcessing.webScraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Optional;

public class WikimediaScraperFunctions
{
    public static void scrapeGeneralData(Document htmlContent)
    {
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
                    System.out.println("Rarity : " + Optional.ofNullable(specifiedRow.get(1).selectFirst("img"))
                            .map(img -> img.attr("alt"))
                            .orElse("Image not found"));
                    System.out.println("World : " + Optional.ofNullable(specifiedRow.get(3).selectFirst("img"))
                            .map(img -> img.attr("alt"))
                            .orElse("Image not found"));
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
            if (row.select("ul span").text().contains("increasing Sanity")) {
                System.out.println("Sanity+:");
                // Może jakiegoś splita mogę zrobić przed słowem 'Increase'
                sanityBox = row.child(1);
                Elements sanityEffects = sanityBox.getElementsByTag("li");
                for(Element sanityEffect : sanityEffects)
                {
                    System.out.println(sanityEffect.text());

                }
            }
            if (row.select("ul span").text().contains("decreasing Sanity")) {
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
        List<String> iconWhiteList = List.of("Wrath1.png","Wrath2.png","Wrath3.png",
                "Lust1.png", "Lust2.png", "Lust3.png",
                "Sloth1.png", "Sloth2.png", "Sloth3.png",
                "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
                "Gloom1.png", "Gloom2.png", "Gloom3.png",
                "Pride1.png", "Pride2.png", "Pride3.png",
                "Envy1.png", "Envy2.png", "Envy3.png");

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
            // Żebym wiedział co się dzieje
            System.out.println("\nPROCESSING:" + element.id());
            // Zwraca pusty pojemnik, jeżeli nie istnieje
            Elements columns = Optional.ofNullable(element.select("table tr").first())
                    .map(row -> row.select("td"))
                    .orElseGet(() -> {
                        System.out.println("No td for table tr");
                        return new Elements();
                    });

            // Z racji, że wiemy, że zawsze będziemy mieć daną kolejnośc kolumn,
            // możemy działać po kolejności pojawienia się
            Element leftAbilityPanel = columns.getFirst();

            for (Element image : leftAbilityPanel.select("img")) {
                String altText = image.attr("alt");
                if (iconWhiteList.contains(altText))
                {
                    System.out.println("Sin affinity: " + altText.replace(".png", ""));
                }
            }
            Element skillIcon = leftAbilityPanel.selectFirst("img[alt$=\" Icon.png\"]");
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
            for (Element image : rightAbilityPanel.select("img")) {
                if (image.attr("alt").contains("Coin")) {
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
    public static void scrapePassiveData(Document htmlContent)
    {
        // Ciekawy styl jest z tym b:contains...
        Elements prePassiveHeaders = htmlContent.select("b:contains(Passive), "
                + "b:contains(Combat Passives), b:contains(Support Passive)");

        for (Element prePassiveHeader : prePassiveHeaders)
        {
            String headerText = prePassiveHeader.text().trim();
            String passiveCategory;
            if (headerText.contains("Combat Passive") || headerText.equals("Passive"))
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
            System.out.println(passiveCategory);
            Element passiveContainer = Optional.ofNullable(prePassiveHeader.parent())
                    .map(Node::nextElementSibling)
                    .orElse(new Element("div"));
            Elements divPassiveContainers = Optional.of(passiveContainer)
                    .map(container -> container.select("div[style*=padding:10px]"))
                    .orElse(new Elements());
            for(Element divPassiveContainer : divPassiveContainers)
            {
                Element costSin = divPassiveContainer.selectFirst("img[alt^=LcbSin]");
                Element costType = divPassiveContainer.selectFirst("span[style*=Mikodacs]");
                if(costType != null && costSin != null)
                {
                    System.out.println("Cost: " + costSin.attr("alt").replace(".png", "") + " " + costType.text());
                    // Irytujący krzyżyk
                    Optional.ofNullable(costSin.nextElementSibling())
                            .ifPresentOrElse(Node::remove,
                                    () -> System.out.println("Cannot remove"));
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
        // corroded ego at 'mw-customcollapsible-corrosion'
        // awakened ego at 'mw-customcollapsible-awakening'
        // Na obu spróbować metody łapania elementu po divie o elemencie z <b> Skills <b>

        // Informacje o kosztach i sin defense w 'mw-content-text'
        // Kompletnie inna struktura niż w id
        // Podobnie jak wcześniej korzystaj z szukania po elemencie który ma tekst z X

        // Pasywki też są inaczej???????????
        System.out.println("test");
    }

    private static String[] longTextSlasher(Element divPassiveContainer) {
        divPassiveContainer.select("br").before("|||");
        String rawSplitPassive = divPassiveContainer.text();
        String[] splitPassive = rawSplitPassive.split("\\|\\|\\|");
        for (String passiveLine : splitPassive) {
            if (!passiveLine.trim().isEmpty()) {
                System.out.println(passiveLine.trim());
            }
        }
        return splitPassive;
    }
}
