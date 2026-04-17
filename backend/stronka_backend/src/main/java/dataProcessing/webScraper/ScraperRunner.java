package dataProcessing.webScraper;

import dataProcessing.services.SinnerService;
import dataProcessing.webScraper.exceptions.ScraperException;
import org.jsoup.nodes.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static dataProcessing.webScraper.DataOperator.*;

@Component
/*
    CommandLineRunner to interface, który pozwala na wykonanie kodu, kiedy kontekst aplikacji spring zostanie
    zainicjalizowany. Zawiera tylko jedną metodę run()
 */
public class ScraperRunner implements CommandLineRunner
{
    private final StatusEffectsScraping statusEffectsScraper;
    private final DataOperator dataOperator;
    private final SinnerService sinnerService;

    public ScraperRunner(StatusEffectsScraping statusEffectsScraper, DataOperator dataOperator, SinnerService sinnerService)
    {
        this.statusEffectsScraper = statusEffectsScraper;
        this.dataOperator = dataOperator;
        this.sinnerService = sinnerService;
    }

    @Override
    /*
        Metoda run jest wykonywana w momencie inicjalizacji kontekstu springa
     */
    public void run(String... args)
    {
        {
            try
            {
                // Jeden do testowania pojedynczej strony, drugi do zbierania linków do pojedynczych stron
                // Struktura stronek do EGO i ID jest bardzo podobna, ale będę się bawił ze status effectami
                // Później jakoś osobno będę musiał przejść przez https://limbuscompany.wiki.gg/wiki/Status_Effects
                // Teoretycznie mogę zrobić Listę list, i działać na indeksach, ale wydaje się to trochę głupie
                List<String> urls = List.of(
                            ("https://limbuscompany.wiki.gg/wiki/Category:Identities"),
                            ("https://limbuscompany.wiki.gg/wiki/Category:E.G.O"));

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
                    statusEffectsScraper.scrapeStatusEffectData(selectedPage);
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
                    try {
                        selectedPage = scrapeData(urlDocument);
                        if (selectedPage != null) {
                            dataOperator.parseData(selectedPage);
                        }
                    }
                    catch(ScraperException e)
                    {
                        System.out.println("Caught error at: " + e);
                        continue;
                    }
                    // Jak za szybko będziemy przechodzić, to otrzymamy status: '429 Too Many Requests'
                    try
                    {
                        Thread.sleep(3000);
                    }
                    catch (InterruptedException _) {}
                }
                sinnerService.buildSinners();
            }
            catch (Throwable t)
            {
                // TODO dodać lepszy debugging
                t.printStackTrace();
            }
            // TODO Więc tutaj bym musiał obsłużyć komunikację pomiędzy backiem i endem, albo zacząć przynajmniej?
        }
    }
}
