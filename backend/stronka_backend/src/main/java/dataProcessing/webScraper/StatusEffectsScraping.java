package dataProcessing.webScraper;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.StatusEffectService;
import dataProcessing.webScraper.utils.ImageScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class StatusEffectsScraping
{
    private final StatusEffectService statusEffectService;

    public StatusEffectsScraping(StatusEffectService statusEffectService)
    {
        this.statusEffectService = statusEffectService;
    }

    /**
     * Metoda odpowiednia za zebranie wszystkich status-effect'ów, przed faktycznym scrapowaniem postaci.
     * Bez wykorzystania jej na samym początku, powstałyby problemy z poprawnym wstawieniem danych do bazy danych
     * @param htmlContent Strona zawierająca wszystkie status-effecty w grze
     */
    public void scrapeStatusEffectData(Document htmlContent)
    {
        Elements statusEffectsTables = htmlContent.select("table").select(".mw-collapsible.article-table");
        for(Element statusEffectsTable : statusEffectsTables)
        {
            Elements statusEffects = statusEffectsTable.select("tr:not(:has(th[style]))");
            for(Element statusEffect : statusEffects)
            {
                statusEffectService.saveNewStatusEffect(extractStatusEffect(statusEffect));
            }
        }
    }

    /**
     * Metoda odpowiednia za stworzenie pojedynczej instancji {@link StatusEffectNodeVisitor}
     * @param rootNode Obecny wiersz z jednym status-effectem
     * @return Rekord pojedynczego status-effect'u
     */
    public static ScraperDataDTOs.StatusEffect extractStatusEffect(Node rootNode)
    {
        StatusEffectNodeVisitor visitor = new StatusEffectNodeVisitor();
        rootNode.traverse(visitor);
        return visitor.getStatusEffect();
    }

    /**
     * Klasa prywatna implementująca metody odpowiednie za zebranie wszystkich informacji o jednym status-effect'cie
     */
    private static class StatusEffectNodeVisitor implements NodeVisitor
    {
        // Buffory są potrzebne z racji tego, jak działa NodeVisitor
        private final StringBuilder titleBuffer = new StringBuilder();
        private final StringBuilder iconBuffer = new StringBuilder();

        private final StringBuilder descriptionBuffer = new StringBuilder();

        // Najłatwiejszy sposób na zdecydowanie co gdzie wkleić, nasza linijka posiada n pojemników, lecz nas interesują
        // tylko pierwsze dwie, (1 oraz 2)
        private int currentBox = 0;
        private ScraperDataDTOs.StatusEffect resultStatusEffect;

        @Override
        public void head(Node node, int depth)
        {
            if (node instanceof TextNode textNode)
            {
                String textNodeText = textNode.getWholeText();
                if (currentBox == 1) {
                    titleBuffer.append(textNodeText);
                }
                else if (currentBox == 2) {
                    // Nie dodajemy do listy z racji samego działania NodeVisitora, inaczej byśmy mieli 15 różnych indeksów
                    // dla jednego zdania, więc lepiej wszystko podzielić dopiero po otrzymaniu całego tekstu
                    descriptionBuffer.append(textNodeText);
                }
            }

            else if (node instanceof Element element)
            {
                if (element.tagName().equals("td"))
                {
                    currentBox += 1;
                }

                // Wracając do tego, co jest napisane powyżej, w bufforze ręcznie dodajemy znaki nowej linii, tak by
                // później w liście wklejać linijki, właśnie po znakach nowej linii
                else if (currentBox == 2 && element.tagName().equals("br"))
                {
                    descriptionBuffer.append("\n");
                }

                else if (element.tagName().equals("img"))
                {
                    String icon = element.attr("alt");
                    if (currentBox == 1)
                    {
                        ImageScraper.scrapeImageURL(element);
                        iconBuffer.append(icon);
                    }

                    else if (currentBox == 2)
                    {
                        ImageScraper.scrapeImageURL(element);
                    }
                }
            }
        }

        @Override
        public void tail(Node node, int depth)
        {
            if (depth == 0)
            {
                String finalName = titleBuffer.toString().trim().replaceAll("\\s+", " ");
                String finalIcon = iconBuffer.toString().trim();

                // Dopiero teraz przetwarzamy bufor i wklejamy jego zawartości do listy
                // Jest to stream, na tablicy rozdzielonego buffora na znakach newline
                // Pierw mapujemy (operujemy/modyfikujemy) wartości za pomocą trim,
                // po czym pozbywamy się linii, które są puste,
                // na końcu wstawiamy je do listy.
                List<String> finalDescriptionLines = Arrays.stream(descriptionBuffer.toString().split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();

                log.info("Name: {}", finalName);
                log.info("Icon: {}", finalIcon);
                log.info("Description: ");
                for (String line : finalDescriptionLines)
                {
                    log.info(line);
                }
                log.info("Related effects: ");

                this.resultStatusEffect = new ScraperDataDTOs.StatusEffect(
                        finalName,
                        finalIcon,
                        finalDescriptionLines);
            }
        }

        public ScraperDataDTOs.StatusEffect getStatusEffect()
        {
            return this.resultStatusEffect;
        }
    }
}
