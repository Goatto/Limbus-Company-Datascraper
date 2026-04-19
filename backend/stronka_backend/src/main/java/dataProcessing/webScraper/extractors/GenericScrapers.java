package dataProcessing.webScraper.extractors;

import dataProcessing.DTOBuilders;
import dataProcessing.webScraper.exceptions.MissingTextException;
import dataProcessing.webScraper.extractors.utils.ScraperConstants;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GenericScrapers
{
    public void scrapeName(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        Element titleSelector = htmlContent.selectFirst(".mw-page-title-main");
        if (titleSelector == null)
        {
            throw new MissingTextException("titleSelector");
        }
        String unitTitle = titleSelector.text();

        boolean sinnerFound = false;
        for (String sinner : ScraperConstants.sinnerWhiteList)
        {
            if (unitTitle.contains(sinner))
            {
                log.info("Sinner: {}", sinner);
                builder.setSinnerName(sinner);
                sinnerFound = true;
                builder.setName(unitTitle);
                log.info(unitTitle);
                break;
            }
        }
        if (!sinnerFound)
        {
            scrapeBackupName(htmlContent, builder);
        }
    }

    public void scrapeBackupName(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        String url = htmlContent.location();
        if (url.contains("/wiki/")) {
            url = url.substring(url.lastIndexOf("/wiki/") + 6);
        }

        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String formattedName = decodedUrl.replace("_", " ");
        log.info("BACKUP Title: {}", formattedName);
        builder.setName(formattedName);
        for (String sinner : ScraperConstants.sinnerWhiteList)
        {
            if (formattedName.contains(sinner))
            {
                log.info("BACKUP Sinner: {}", sinner);
                builder.setSinnerName(sinner);
                break;
            }
        }
    }

    public void parseResistances(String text, String[] resistanceNames, DTOBuilders.BaseEquippableBuilder<?> builder)
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
                if (!cleanNumber.isEmpty() && index < resistanceNames.length) {
                    // Zamieniamy na double
                    double multiplier = Double.parseDouble(cleanNumber);
                    // Dodajemy do buildera korzystając z nazwy z tablicy
                    builder.addResistance(resistanceNames[index], multiplier);
                    System.out.print(resistance + " ");
                    index++;
                }
            }
        }
    }
}