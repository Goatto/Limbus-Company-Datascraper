package dataProcessing.webScraper.enums;

import dataProcessing.ScraperDataDTOs;

/**
 * Reprezentuje stopień EGO, wykorzystywane w rekordzie {@link ScraperDataDTOs.EGOData}
 */
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
            // Zabezpieczenie przed NullPointerException
            case null, default -> UNKNOWN;
        };
    }
}