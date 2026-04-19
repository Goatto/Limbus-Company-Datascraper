package dataProcessing.webScraper.extractors.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScraperConstants
{

    public static Set<String> iconWhiteList = Set.of("Wrath1.png","Wrath2.png","Wrath3.png",
            "Lust1.png", "Lust2.png", "Lust3.png",
            "Sloth1.png", "Sloth2.png", "Sloth3.png",
            "Gluttony1.png", "Gluttony2.png", "Gluttony3.png",
            "Gloom1.png", "Gloom2.png", "Gloom3.png",
            "Pride1.png", "Pride2.png", "Pride3.png",
            "Envy1.png", "Envy2.png", "Envy3.png");
    public static List<String> sinnerWhiteList = List.of("Yi Sang", "Faust", "Don Quixote", "Ryōshū", "Ryoshu",
                    "Meursault", "Hong Lu", "Heathcliff", "Ishmael", "Rodion", "Sinclair", "Outis", "Gregor");
    public static String whitelistSelector = sinnerWhiteList.stream()
                            .map(sinner -> "img[alt*=\"" + sinner + "\"]")
                            .collect(Collectors.joining(", "));
    public static String[] egoResistances = {"Wrath", "Lust", "Sloth", "Gluttony", "Gloom", "Pride", "Envy"};
}
