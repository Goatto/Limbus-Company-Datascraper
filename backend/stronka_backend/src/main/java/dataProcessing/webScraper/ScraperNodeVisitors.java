package dataProcessing.webScraper;

import dataProcessing.ScraperDataDTOs;
import lombok.Getter;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.util.*;
import java.util.regex.Pattern;

// FIXME Poprawić jak zbierasz status-effecty
public class ScraperNodeVisitors
{
    /**
     * Metoda odpowiednia za stworzenie pojedynczej instancji {@link PassiveNodeVisitor}
     * @param rootNode Element zawierający umiejętności pasywne
     * @return Rekord jednej umiejętności pasywnej
     */
    public static ScraperDataDTOs.Passive extractPassive(Node rootNode)
    {
        PassiveNodeVisitor visitor = new PassiveNodeVisitor();
        rootNode.traverse(visitor);
        return visitor.getBuiltPassive();
    }

    /**
     * Klasa prywatna implementująca metody odpowiednie za zebranie wszystkich informacji o jednej umiejętności pasywnej
     */
    private static class PassiveNodeVisitor implements NodeVisitor
    {

        private final StringBuilder titleBuffer = new StringBuilder();
        private final Map<String, Integer> costBuffer = new HashMap<>();
        private final StringBuilder descriptionBuffer = new StringBuilder();
        // TODO zaimplementować zbieranie status-effect'ów po zbudowaniu podstawy springa
        private final Set<String> statusEffectsBuffer = new HashSet<>();

        // Flagi na to, w jakim z fragmentów jestesmy, mamy 3 gdyz mozemy rodzielic umiejetnosc pasywna
        // na 3 wazne informacje. Jej tytuł, jej koszt (jeżeli taki istnieje) oraz jej opis
        int titleDepth = 0;
        boolean isCost = false;
        String currentSinForCost = "";
        String costType = "";
        boolean isTheAnnoyingX = false;

        private ScraperDataDTOs.Passive resultPassive;

        @Override
        public void head(Node node, int depth)
        {
            switch (node)
            {
                case Element element when element.is("div.skillgrad-font") -> titleDepth += 1;
                case Element element when element.is("span[style*=color:#a0693b]") -> isTheAnnoyingX = true;
                case Element element when element.tagName().equals("br") ->
                {
                    if(isCost)
                    {
                        isCost = false;
                    }
                    else
                    {
                        descriptionBuffer.append("\n");
                    }
                }
                case Element element when element.hasAttr("alt") ->
                {
                    String altText = element.attr("alt");

                    if (altText.contains("LcbSin"))
                    {
                        isCost = true;
                        currentSinForCost = altText;
                    }
                    else if (!altText.isEmpty())
                    {
                        statusEffectsBuffer.add(altText.replace(".png", "").trim());
                    }
                }
                default -> {}
            }

            // Wykorzystujemy patternVariable zamiast ręcznej inicjalizacji
            if(node instanceof TextNode textNode)
            {
                if(isTheAnnoyingX)
                {
                    return;
                }
                // Rzutowanie naszego obecnego node na TextNode
                String nodeTextContent = textNode.getWholeText();
                if(!nodeTextContent.isEmpty())
                {
                    if(titleDepth > 0)
                    {
                        titleBuffer.append(nodeTextContent);
                    }
                    else if(isCost)
                    {
                        String nodeTextContentDigits = nodeTextContent.replaceAll("\\D", "");
                        if(!nodeTextContentDigits.isEmpty())
                        {
                            costBuffer.put(currentSinForCost, Integer.parseInt(nodeTextContentDigits));
                        }
                        if(nodeTextContent.toLowerCase().contains("owned"))
                        {
                            costType = "Owned";
                        }
                        else if(nodeTextContent.toLowerCase().contains("res"))
                        {
                            costType = "Resonance";
                        }

                    }
                    else
                    {
                        descriptionBuffer.append(nodeTextContent);
                    }
                }
            }
        }
        @Override
        public void tail(Node node, int depth)
        {
            // Odpowiada też za cleanup
            if(node instanceof Element && ((Element) node).is("div.skillgrad-font"))
            {
                titleDepth -= 1;
            }

            if(node instanceof Element && ((Element) node).is("span[style*=color:#a0693b]"))
            {
                isTheAnnoyingX = false;
            }

            // depth odpowiada za to, jak głęboko jesteśmy w elemencie, 0 oznacza, że z niego wysiedliśmy
            if(depth == 0)
            {
                String finalTitle = titleBuffer.toString().trim().replaceAll("\\s+", " ");
                String finalCost = costBuffer.toString().trim().replaceAll("\\s+", " ");
                String finalCostType = costType.isEmpty() ? "None" : costType;
                String finalDescription = descriptionBuffer.toString()
                                            .replaceAll("[ \\t\\u00A0]+", " ").trim();
                List<String> finalEffects = finalDescription.isEmpty()
                        ? new ArrayList<>()
                        : Arrays.stream(finalDescription.split("\n"))
                                .map(String::trim)
                                .filter(line -> !line.isEmpty())
                                .toList();

                System.out.println("Passive title: " + finalTitle);
                System.out.println("Cost: " + finalCost);
                System.out.println("Cost type: " + finalCostType);
                System.out.println("Description: ");
                for(String descriptionLine : finalEffects)
                {
                    System.out.println(descriptionLine);
                }

                this.resultPassive = new ScraperDataDTOs.Passive(
                        finalTitle,
                        finalCostType,
                        costBuffer,
                        finalEffects,
                        statusEffectsBuffer);
            }
        }
        private ScraperDataDTOs.Passive getBuiltPassive()
        {
            System.out.println(statusEffectsBuffer);
            return this.resultPassive;
        }
    }

    public record AbilityResult(
            List<String> baseEffects,
            Map<String, List<String>> coinEffects,
            Set<String> statusEffects
    ) {}

    /**
     * Metoda odpowiednia za stworzenie pojedynczej instancji {@link AbilityNodeVisitor}
     * @param rootNode Element zawierający jedną umiejętność
     * @return Rekord efektów jednej umiejętności
     */
    public static AbilityResult extractAbility(Node rootNode)
    {
        AbilityNodeVisitor visitor = new AbilityNodeVisitor();
        rootNode.traverse(visitor);
        return visitor.getResult();
    }
    /**
     * Klasa prywatna implementująca metody odpowiednie za zebranie wszystkich informacji o jednej umiejętności
     */
    private static class AbilityNodeVisitor implements NodeVisitor
    {
        // Zamiast za każdym razem generować regex od nowa, robimy to raz na początku
        private static final Pattern statRegex = Pattern.compile(".*\\d+\\s*\\(\\d+\\s*[+-]\\d+\\).*");
        private static final Pattern amountRegex = Pattern.compile("^x\\d+$");

        // Automatycznie wygenerowane gettery przez Lomboka
        @Getter
        private final List<String> baseEffects = new ArrayList<>();
        @Getter
        private final Map<String, List<String>> coinEffects = new HashMap<>();
        @Getter
        private final Set<String> statusEffects = new HashSet<>();

        // Buffor odpowiedni za składanie tekstu w jedną linię
        private final StringBuilder currentLineBuffer = new StringBuilder();

        private boolean isACoin = false;
        private String currentCoinNumber = null;
        // Flaga pomijająca nazwę skilla itp.
        private boolean passHeader = false;

        @Override
        public void head(Node node, int depth)
        {
            if(node instanceof TextNode textNode)
            {
                String rawText = textNode.getWholeText().trim();

                if(rawText.isEmpty())
                {
                    return;
                }

                boolean isStat = rawText.contains("Atk Weight") ||
                        statRegex.matcher(rawText).matches() ||
                        rawText.equals("Amt.") ||
                        amountRegex.matcher(rawText).matches() ||
                        rawText.equals("?") ||
                        rawText.equals("-");

                if(isStat)
                   {
                       return;
                   }

                if(!passHeader)
                {
                    passHeader = true;
                    return;
                }

                String text = textNode.getWholeText().trim();
                if(!currentLineBuffer.isEmpty() && !currentLineBuffer.toString().endsWith(" "))
                {
                    currentLineBuffer.append(" ");
                }
                currentLineBuffer.append(text);
            }

            else if(node instanceof Element element)
            {
                if((element.is("div span") || element.is("font")) && !passHeader)
                {
                    return;
                }

                if(element.is("div[style*=\"padding-left\"]"))
                {
                    saveCurrentLine();
                    isACoin = true;
                    passHeader = true;
                    Element coinImage = element.selectFirst("img[alt^=\"CoinEffect\"]");
                    if(coinImage != null)
                    {
                        currentCoinNumber=coinImage.attr("alt");
                    }
                }
                else if(element.tagName().equals("b"))
                {
                    currentLineBuffer.append(" ");
                }
                else if(element.tagName().equals("br"))
                {
                    saveCurrentLine();
                }
                else if(element.tagName().equals("img") && !element.attr("alt").contains("oin.png")
                && !element.attr("alt").contains("CoinEffect") && !element.attr("alt").contains("SkillAttack"))
                {
                    statusEffects.add(element.attr("alt").replace(".png", "").trim());
                }
            }
        }
        @Override
        public void tail(Node node, int depth)
        {
            if(node instanceof Element element && element.is("div[style*=\"padding-left\"]"))
            {
                saveCurrentLine();
                isACoin = false;
                currentCoinNumber = null;
            }

            if(depth == 0)
            {
                saveCurrentLine();
            }
        }

        private void saveCurrentLine()
        {
            String line = currentLineBuffer.toString()
                    .replaceAll("[ \t\u00A0]+", " ").trim();
            if(!line.isEmpty())
            {
                if(isACoin && currentCoinNumber != null)
                {
                    // Tworzymy array dla linijki tekstu monety
                    coinEffects.computeIfAbsent(currentCoinNumber, _ -> new ArrayList<>()).add(line);
                }
                else
                {
                    baseEffects.add(line);
                }
            }
            currentLineBuffer.setLength(0);
        }

        public AbilityResult getResult()
        {
            System.out.println(statusEffects);
            return new AbilityResult(getBaseEffects(), getCoinEffects(), getStatusEffects());
        }
    }
}
