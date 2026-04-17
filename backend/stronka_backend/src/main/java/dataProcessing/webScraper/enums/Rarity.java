package dataProcessing.webScraper.enums;

import dataProcessing.ScraperDataDTOs;

/**
 * Reprezentuje rzadkość ID, wykorzystywane w rekordzie {@link ScraperDataDTOs.IDData}
 */
public enum Rarity
{
    ONE_STAR, TWO_STAR, THREE_STAR, UNKNOWN;

    public static Rarity rarityParser(String text)
    {
        return switch (text)
        {
            case String s when s.contains("1") -> ONE_STAR;
            case String s when s.contains("2") -> TWO_STAR;
            case String s when s.contains("3") -> THREE_STAR;
            // Zabezpieczenie przed NullPointerException
            case null, default -> UNKNOWN;
        };
    }
}