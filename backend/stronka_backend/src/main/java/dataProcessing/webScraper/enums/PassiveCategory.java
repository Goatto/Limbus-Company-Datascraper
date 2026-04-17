package dataProcessing.webScraper.enums;

public enum PassiveCategory
{
    COMBAT_PASSIVE, SUPPORT_PASSIVE, UNKNOWN;

    public static PassiveCategory passiveParser(String text)
    {
        return switch (text)
        {
            case String s when s.contains("Combat Passive") || s.equals("Passives") || s.equals("Passive") -> COMBAT_PASSIVE;
            case String s when s.contains("Support Passive") -> SUPPORT_PASSIVE;
            case null, default -> UNKNOWN;
        };
    }
}
