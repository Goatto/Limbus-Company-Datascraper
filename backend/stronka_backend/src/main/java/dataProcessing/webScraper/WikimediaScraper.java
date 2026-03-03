package dataProcessing.webScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikimediaScraper
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
            List<String> urls = List.of(("https://limbuscompany.wiki.gg/wiki/Category:Identities"), ("https://limbuscompany.wiki.gg/wiki/Category:E.G.O"));
            // List<String> url = List.of("https://limbuscompany.wiki.gg/wiki/LCB_Sinner_Yi_Sang");

            List<String> urlLists = linkScraper(urls);
            // Ręcznie dodajemy Status_Effect, bo czemu nie
            urlLists.add("https://limbuscompany.wiki.gg/wiki/Status_Effects");
            for(String urlDocument : urlLists)
            {
                Document selectedPage = scrapeData(urlDocument);
                if (selectedPage != null)
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
            // Później jakiś lepszy debugging zrobię
            t.printStackTrace();
        }
    }

    // Na razie do sprawdzenia jak zbiera się linki, później będę musiał to inaczej zrobić z racji,
    // że każda stronka ma inną strukturę więc parser będzie musiał brać to pod uwagę
    private static List<String> linkScraper(List<String> categories)
    {
        List<String> outputLinks = new ArrayList<>();
        // Whitelist z racji, że w nazwach ID TAKŻE mamy nazwy jednostki, więc będzie mniej brudu
        List<String> whiteList = List.of("Yi Sang", "Faust", "Don Quixote", "Ryōshū", "Meursault", "Hong Lu",
                "Heathcliff", "Ishmael", "Rodion", "Sinclair", "Outis", "Gregor");

        for(String category : categories)
        {
            Document htmlContent = scrapeData(category);
            if(htmlContent!=null)
            {
                Elements unitLinks = htmlContent.select("#mw-pages .mw-content-ltr .mw-category .mw-category-group a");
                for (Element unitLink : unitLinks)
                {
                    String unitTitle = unitLink.attr("title");
                    // Przechodzimy przez białą listę, pewnie da się to lepiej zrobić, ale nie będę się męczył
                    // Dopiero poprawie, jeżeli pojawią się problemy optymalizacyjne
                    for (String sinner : whiteList)
                    {
                        if (unitTitle.contains(sinner))
                        {
                            // System.out.println(unitLink.attr("title"));
                            String unitURL = unitLink.attr("abs:href");
                            outputLinks.add(unitURL);
                            break;
                        }
                    }
                }
                // System.out.println("\n");
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
            Element smallSelector;
            Elements largeSelector;
            if(category.text().equals("E.G.O") || category.text().equals("Identities"))
            {

                smallSelector = htmlContent.selectFirst(".mw-page-title-main");
                if(smallSelector != null)
                {
                    System.out.println(smallSelector.text());
                }
                else
                {
                    System.out.println("Nie znaleziono title");
                }

                smallSelector = htmlContent.selectFirst(".mw-collapsible-content a");
                if(smallSelector != null)
                {
                    System.out.println(smallSelector.attr("abs:href"));
                }
                else
                {
                    System.out.println("Nie znaleziono portretu");
                }

                largeSelector = htmlContent.select("#General_Info-0 table tr");
                for(Element row : largeSelector)
                {
                    Elements specifiedRow = row.select("td");
                    int specifiedRowSize = specifiedRow.size();
                    // Zawsze treshholdy staggera
                    if(specifiedRowSize == 1)
                    {
                        System.out.println(specifiedRow.getFirst().text());
                    }
                    // Zawsze tylko traits
                    // Wykombinować jak to rozdzielić, pewnie będzie to jakaś lista stringów w klasie wsmie
                    if(specifiedRowSize == 2)
                    {
                        System.out.print(specifiedRow.get(1).text() + '\n');
                    }
                    if(specifiedRowSize == 3 && !specifiedRow.getFirst().text().isEmpty())
                    {
                        System.out.print(specifiedRow.getFirst().text() + ' ');
                        System.out.print(specifiedRow.get(1).text() + ' ');
                        System.out.print(specifiedRow.get(2).text() + '\n');
                    }
                    // Będę musiał jakoś rozróżniać te wartości, pewnie do zmiennych je i będę sprawdzał
                    // Świat moge rozpoznać po tym, że pierwsze słowo to 'World'
                    // Rzadkość po tym, że jego plik to IDNumber...
                    // Sezon po słowie Season
                    // Release po formacie XXXX.XX.XX

                    if(specifiedRowSize == 4)
                    {
                        if(!specifiedRow.get(1).text().isEmpty())
                        {
                            System.out.print(specifiedRow.get(1).text() + ' ');
                        }
                        else
                        {
                            System.out.print(specifiedRow.get(1).selectFirst("img").attr("alt") + ' ');
                        }
                        if(!specifiedRow.get(3).text().isEmpty())
                        {
                            System.out.print(specifiedRow.get(3).text() + '\n');
                        }
                        else
                        {
                            System.out.print(specifiedRow.get(3).selectFirst("img").attr("alt") + '\n');
                        }
                    }
                    // Zawsze życie + speed + def. level
                    if(specifiedRowSize == 6)
                    {
                        System.out.print(specifiedRow.get(1).text() + ' ');
                        System.out.print(specifiedRow.get(3).text() + ' ');
                        System.out.print(specifiedRow.get(5).text() + ' ');
                    }

                    // Zawsze SinResistnace z ego
                    if(specifiedRowSize == 7)
                    {
                        System.out.println("TODO");
                    }
                }
                // Sanity, tylko ID je mają więc sprawdzamy
                if(category.text().equals("Identities"))
                {
                    largeSelector = htmlContent.select("#Sanity-0 table tr td div");
                    for(Element row : largeSelector)
                    {
                        // Muszę teraz jakoś przejść przez wszystkie linijki i je połączyć sensownie
                        if(row.select("ul span b").text().contains("increasing Sanity"))
                        {
                            System.out.println("+Saniity");
                        }
                        if(row.select("ul span b").text().contains("decreasing Sanity"))
                        {
                            System.out.println("-Saniity");
                        }
                    }
                }
                break;
            }
            if(category.text().equals("Status Effect Pages"))
            {
                System.out.println("STATUS EFFECT");
                break;
            }
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
                "Upgrade-Insecure-Requests", "1"
        );
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
