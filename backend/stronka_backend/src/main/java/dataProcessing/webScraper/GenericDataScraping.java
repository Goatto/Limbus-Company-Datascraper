package dataProcessing.webScraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

public class GenericDataScraping
{
    /**
     * Metoda odpowiednia za zebranie generycznych danych i.e. takich, które pojawiają się wielokrotnie na innych stronach.
     * Wykorzystuje {@link #scrapeIcon(Element)} do pobrania ikonek ów danych generycznych
     * @param htmlContent Document HTML strony generycznej
     */
    public static void checkPageType(Document htmlContent)
    {
        // Takie index istnieje tylko na stronach ID, więc jeżeli go otrzymamy wiemy, że jesteśmy na ID
        Element idTable = htmlContent.selectFirst("#General_Info-1 table");
        if (idTable != null)
        {
            // Przekazujemy poszczególne elementy do funkcji pomocniczej, która jest odpowiedzialna za przetworzenie ich
            scrapeIcon(idTable.selectFirst("td:contains(Rarity)"));
            scrapeIcon(idTable.selectFirst("tr:has(th:contains(Status)), tr:has(td:contains(Status))"));
            scrapeIcon(idTable.selectFirst("tr:has(th:contains(Resistances)), tr:has(td:contains(Resistances))"));

        }
        else
        {
            // Z racji, że na liście wejściowej znajdują się tylko stronki z ID lub EGO, jeżeli pierwszy warunek nie jest
            // spełniony, wiemy, że jesteśmy na stronie EGO
            Element contentArea = htmlContent.selectFirst(".mw-body-content");
            if (contentArea != null)
            {
                scrapeIcon(contentArea.selectFirst("table:has(b:contains(Info)) td:contains(Risk Level)"));
                scrapeIcon(contentArea.selectFirst("table:has(b:contains(Cost)) td:contains(Sanity)"));
                scrapeIcon(contentArea.selectFirst("table tr:has(th:contains(Resistances))"));
            }
        }
    }

    /**
     * Metoda odpowiednia za pobieranie ikonek danych generycznych, wykorzystuje {@link ImageScraper#scrapeImageURL(Element)}.
     * @param scrapedElement Element, z którego chcemy wydobyć obraz
     */
    private static void scrapeIcon(Element scrapedElement)
    {
        // Tworzymy pojemnik na naszą wartość, jeżeli w jakiejkolwiek chwili wyjdzie na to, że jest nullem
        // program się nie wyrzuci, a sama operacja nic nie zrobi
        Optional.ofNullable(scrapedElement)
                // Przechodzimy do następnego elementu
                .map(Element::nextElementSibling)
                // Wybieramy z elementu obrazy
                .map(sibling -> sibling.select("img"))
                // Jeżeli wartość nie jest nullem, dla każdego obrazu wykonujemy metodę ImageScraper.scrapeImageURL
                .ifPresent(images -> images.forEach(ImageScraper::scrapeImageURL));
    }
}