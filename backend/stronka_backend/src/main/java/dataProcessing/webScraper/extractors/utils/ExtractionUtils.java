package dataProcessing.webScraper.extractors.utils;

import dataProcessing.webScraper.exceptions.MissingImageException;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import dataProcessing.webScraper.exceptions.MissingTextException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractionUtils {
    public static Element extractElementFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingSectionException(fieldName);
        }
        return columns.get(columnIndex);
    }

    public static String extractAltFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingImageException(fieldName);
        }
        Element img = columns.get(columnIndex).selectFirst("img");
        if(img == null)
        {
            throw new MissingImageException(fieldName);
        }
        return img.attr("alt");
    }

    public static String extractTextFromSpecifiedIndex(Elements columns, int columnIndex, String fieldName)
    {
        if(columnIndex >= columns.size())
        {
            throw new MissingSectionException(fieldName);
        }
        String text = columns.get(columnIndex).text().trim();
        if(text.isEmpty())
        {
            throw new MissingTextException(fieldName);
        }
        return text;
    }
}
