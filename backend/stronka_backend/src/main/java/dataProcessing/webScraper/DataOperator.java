package dataProcessing.webScraper;

import dataProcessing.DTOBuilders;
import dataProcessing.services.EGOService;
import dataProcessing.services.IDService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataOperator
{
    private final WikimediaScraper wikimediaScraper;
    private final EGOService EGOService;
    private final IDService IDService;
    private static final int retryCount = 3;

    public DataOperator(WikimediaScraper wikimediaScraper, EGOService egoService, IDService idService)
    {
        this.wikimediaScraper = wikimediaScraper;
        EGOService = egoService;
        IDService = idService;
    }

    // Na razie do sprawdzenia jak zbiera się linki, później będę musiał to inaczej zrobić z racji,
    // że każda stronka ma inną strukturę więc parser będzie musiał brać to pod uwagę
    static List<String> linkScraper(List<String> categories)
    {
        List<String> outputLinks = new ArrayList<>();
        // Whitelist z racji, że w nazwach ID TAKŻE mamy nazwy jednostki, więc będzie mniej brudu
        List<String> sinnerWhiteList = List.of("Yi Sang", "Faust", "Don Quixote", "Ryōshū",
                "Meursault", "Hong Lu", "Heathcliff", "Ishmael", "Rodion", "Sinclair", "Outis", "Gregor");

        for(String category : categories)
        {
            Document htmlContent = scrapeData(category);
            if(htmlContent!=null)
            {
                Elements unitLinks = htmlContent.select("#mw-pages .mw-content-ltr "
                        + ".mw-category .mw-category-group a");
                for (Element unitLink : unitLinks)
                {
                    String unitTitle = unitLink.attr("title");
                    // Przechodzimy przez białą listę, pewnie da się to lepiej zrobić, ale nie będę się męczył
                    // Dopiero poprawie, jeżeli pojawią się problemy optymalizacyjne
                    for (String sinner : sinnerWhiteList)
                    {
                        if (unitTitle.contains(sinner))
                        {
                            String unitURL = unitLink.attr("abs:href");
                            outputLinks.add(unitURL);
                            break;
                        }
                    }
                }
            }
            else
            {
                log.info("Null: {}", category);
            }
        }
        return outputLinks;
    }

    void parseData(Document htmlContent)
    {
        // Już sprawdzamy przed wywołaniem czy htmlContent jest null
        Elements categories = htmlContent.select("#catlinks .mw-normal-catlinks ul li");
        for(Element category : categories)
        {
            String categoryText = category.text();
            if (categoryText.equals("Identities"))
            {
                DTOBuilders.IDDataBuilder builder = new DTOBuilders.IDDataBuilder();

                // Jeżeli w tym miejscu otrzymamy null, strona nie jest skończona, i próba wyjęcia z jej wartości
                // wyrzuci program
                if(WikimediaScraper.scrapeGeneralIDData(htmlContent, builder) == null)
                {
                    break;
                }
                WikimediaScraper.scrapeSanityData(htmlContent, builder);
                wikimediaScraper.scrapeIDAbilityData(htmlContent, builder);
                wikimediaScraper.scrapePassiveData(htmlContent, builder);
                IDService.saveNewID(builder.buildIDData());
            }
            else if (categoryText.equals("E.G.O"))
            {
                DTOBuilders.EGODataBuilder builder = new DTOBuilders.EGODataBuilder();

                // TODO podobne sprawdzenie przed niedokończoną stroną zrobić tu
                if(wikimediaScraper.scrapeEGOAbilities(htmlContent, builder) == null)
                {
                    break;
                }
                wikimediaScraper.scrapePassiveData(htmlContent, builder);
                EGOService.saveNewEGO(builder.buildEGOData());
            }
            break;
        }
    }

    static Document scrapeData(String url)
    {
        Map<String, String> jsoupHeaders = Map.of(
                // Nasz userAgent
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:148.0) Gecko/20100101 Firefox/148.0",
                // Jakie treści akceptujemy
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                // Preferencje językowe
                "Accept-Language", "en-US,en;q=0.9,pl;q=0.8",
                // Skąd się bierzemy, zazwyczaj korzystamy bezpośrednio z google, ale dla bezpieczeństwa użyje wikimedia
                "Referer", "https://limbuscompany.wiki.gg/",
                // Automatycznie przechodzi z http na https, jeżeli może
                "Upgrade-Insecure-Requests", "1");
        for (int currentAttemptCount = 0; currentAttemptCount < retryCount; currentAttemptCount++)
        {
            try
            {
                log.info("Attempt {} to scrape {}", currentAttemptCount + 1, url);
                Document document = Jsoup.connect(url)
                        .timeout(8000)
                        .headers(jsoupHeaders)
                        .get();
                log.info("Scraped {}", url);
                // TODO Nie wiem czy to nie może przypadkiem jakiś problemów sprawić, więc sobie zaznaczam na później
                document.select(".tooltip-contents").remove();
                return document;
            }
            catch (IOException e)
            {
                System.err.println("Attempt " + (currentAttemptCount + 1) + " failed at " + url + ": " + e.getMessage());
                try
                {
                    Thread.sleep(8000);
                }
                catch (InterruptedException _) {}
            }
        }
        return null;
    }
}
