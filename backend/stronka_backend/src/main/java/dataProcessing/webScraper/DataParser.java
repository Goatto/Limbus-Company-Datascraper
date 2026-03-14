package dataProcessing.webScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataParser
{
    private static final int retryCount = 3;
    static void main(String[] args)
    {
        try
        {
            // Jeden do testowania pojedynczej strony, drugi do zbierania linków do pojedynczych stron
            // Struktura stronek do EGO i ID jest bardzo podobna, ale będę się bawił ze status effectami
            // Później jakoś osobno będę musiał przejść przez https://limbuscompany.wiki.gg/wiki/Status_Effects
            // Teoretycznie mogę zrobić Listę list, i działać na indeksach, ale wydaje się to trochę głupie
            List<String> urls = List.of(
                    // ("https://limbuscompany.wiki.gg/wiki/Category:Identities"),
                    ("https://limbuscompany.wiki.gg/wiki/Category:E.G.O"));

            /*TODO:
                Zebrać ikonki grzechów
                Zebbrać ikonki rzadkości
                Zebrać ikonki statusów
                Zebrać ikonki resistances
                Zebrać ikonke sanity
             */
            List<String> genericAssetScraper = List.of(
                    ("https://limbuscompany.wiki.gg/wiki/LCB_Sinner_Yi_Sang"), // 1 gwiazdka
                    ("https://limbuscompany.wiki.gg/wiki/Seven_Assoc._South_Section_6_Yi_Sang"), // 2 gwiazdki
                    ("https://limbuscompany.wiki.gg/wiki/Blade_Lineage_Salsu_Yi_Sang"), // 3 gwiazdki
                    ("https://limbuscompany.wiki.gg/wiki/Crow%27s_Eye_View_Yi_Sang"), // ZAIYN
                    ("https://limbuscompany.wiki.gg/wiki/4th_Match_Flame_Yi_Sang"), // TETH
                    ("https://limbuscompany.wiki.gg/wiki/Dimension_Shredder_Yi_Sang"), // HE
                    ("https://limbuscompany.wiki.gg/wiki/Sunshower_Yi_Sang") // WAW
            );

            String statusEffects = "https://limbuscompany.wiki.gg/wiki/Status_Effects";

            // Odpowiednie za zbieranie 'statycznych danych' i.e. takich danych, które wielokrotnie pojawiają się
            // na różnych stronkach, głównie wykorzystane do pobrania ikonek status effectów
            System.out.println("Scraping status effects: ");
            Document selectedPage = scrapeData(statusEffects);
            if(selectedPage != null)
            {
                StatusEffectsScraping.scrapeStatusEffectData(selectedPage);
            }

            System.out.println("Scraping static pages: ");
            for(String urlDocument : genericAssetScraper)
            {
                selectedPage = scrapeData(urlDocument);
                if(selectedPage != null)
                {
                    GenericDataScraping.checkPageType(selectedPage);
                }
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException _) {}
            }


            // Wyłapanie wszystkich linków z dwóch głównych katalogów
            List<String> urlLists = linkScraper(urls);
            for(String urlDocument : urlLists)
            {
                selectedPage = scrapeData(urlDocument);
                if(selectedPage != null)
                {
                        parseData(selectedPage);
                }
                // Jak za szybko będziemy przechodzić, to otrzymamy status: '429 Too Many Requests'
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException _) {}
            }

        }
        catch (Throwable t)
        {
            // TODO dodać lepszy debugging
            t.printStackTrace();
        }
    }

    // Na razie do sprawdzenia jak zbiera się linki, później będę musiał to inaczej zrobić z racji,
    // że każda stronka ma inną strukturę więc parser będzie musiał brać to pod uwagę
    private static List<String> linkScraper(List<String> categories)
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
                System.out.println("Null: " + category);
            }
        }
        return outputLinks;
    }

    private static void parseData(Document htmlContent)
    {
        // Już sprawdzamy przed wywołaniem czy htmlContent jest null
        Elements categories = htmlContent.select("#catlinks .mw-normal-catlinks ul li");
        for(Element category : categories)
        {
            if(category.text().equals("E.G.O") || category.text().equals("Identities"))
            {
                RecordBuilders.IDDataBuilder builder = new RecordBuilders.IDDataBuilder();
                WikimediaScraper.scrapeGeneralIDData(htmlContent, builder);

                if (category.text().equals("Identities"))
                {
                    // Tylko ID mają sanity
                    // TODO Buildery tutaj są temp, tak by program się nie wykrzaczał
                    WikimediaScraper.scrapeSanityData(htmlContent, new RecordBuilders.IDDataBuilder());
                    // E.G.O przechowują umiejętności i pasywki w inny sposób
                    WikimediaScraper.scrapeIDAbilityData(htmlContent);
                }
                else if(category.text().equals("E.G.O"))
                {
                    WikimediaScraper.scrapeEGOAbilities(htmlContent, new RecordBuilders.EGODataBuilder());
                }
                WikimediaScraper.scrapePassiveData(htmlContent, new RecordBuilders.EGODataBuilder());
            }
            if(category.text().equals("Status Effect Pages"))
            {
                System.out.println("STATUS EFFECT");
            }
            break;
        }
        // Sprawdzamy, która ze pod-stronek to jest
    }

    private static Document scrapeData(String url)
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
                System.out.println("Attempt " + (currentAttemptCount + 1) + " to scrape " + url);
                Document document = Jsoup.connect(url)
                        .timeout(5000)
                        .headers(jsoupHeaders)
                        .get();
                System.out.println("Scraped " + url);
                // TODO Nie wiem czy to nie może przypadkiem jakiś problemów sprawić, więc sobie zaznaczam na później
                document.select(".tooltip-contents").remove();
                return document;
            }
            catch (IOException e)
            {
                System.err.println("Attempt " + (currentAttemptCount + 1) + " failed at " + url + ": " + e.getMessage());
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException _) {}
            }
        }
        return null;
    }
}
