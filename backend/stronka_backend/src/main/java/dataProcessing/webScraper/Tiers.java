package dataProcessing.webScraper;

public class Tiers
{
    public enum Rarity
    {
        ONE_STAR, TWO_STAR, THREE_STAR, UNKOWN;

        public static Rarity rarityParser(String text)
        {
            return switch (text)
            {
                // Zabezpieczenie przed NullPointerException
                case String s when s.contains("1") -> ONE_STAR;
                case String s when s.contains("2") -> TWO_STAR;
                case String s when s.contains("3") -> THREE_STAR;
                case null, default -> UNKOWN;
            };
        }
    }

    public enum ThreatLevel
    {
        ZAYIN, TETH, HE, WAW, ALEPH, UNKNOWN;

        public static ThreatLevel threatLevelParser(String text)
        {
            return switch (text)
            {
                case String s when s.contains("ZAYIN") -> ZAYIN;
                case String s when s.contains("TETH") -> TETH;
                case String s when s.contains("HE") -> HE;
                case String s when s.contains("WAW") -> WAW;
                case String s when s.contains("ALEPH") -> ALEPH;
                case null, default -> UNKNOWN;
            };
        }
    }
}
