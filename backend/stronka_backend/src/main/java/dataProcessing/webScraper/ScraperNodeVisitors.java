package dataProcessing.webScraper;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScraperNodeVisitors
{
    /* TODO, do złapania:
        Nazwa pasywki,
        jej koszt,
        opis,
        status effecty zawarte w opisie
    */
    public static class PassiveNodeVisitor implements NodeVisitor
    {

        private final StringBuilder titleBuffer = new StringBuilder();
        private final Map<String, Integer> costBuffer = new HashMap<>();
        private final StringBuilder descriptionBuffer = new StringBuilder();

        // Flagi na to, w jakim z fragmentów jestesmy, mamy 3 gdyz mozemy rodzielic umiejetnosc pasywna
        // na 3 wazne informacje. Jej tytuł, jej koszt (jeżeli taki istnieje) oraz jej opis
        int titleDepth = 0;
        boolean isCost = false;
        String currentSinForCost = "";
        String costType = "";
        boolean isTheAnnoyingX = false;

        private FormatedScraperData.Passive resultPassive;

        @Override
        public void head(Node node, int depth)
        {
            switch (node)
            {
                case Element element when element.is("div.skillgrad-font") -> titleDepth += 1;
                case Element element when element.is("span[style*=color:#a0693b]") -> isTheAnnoyingX = true;
                case Element element when element.attr("alt").contains("LcbSin") ->
                {
                    isCost = true;
                    currentSinForCost = node.attr("alt");
                }
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
            else if(node instanceof Element elementNode)
            {
                if(elementNode.tagName().equals("img"))
                {
                    String elementNodeText = elementNode.attr("alt");
                    if(isCost)
                    {
                        currentSinForCost = elementNodeText;
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
                String finalDescription = descriptionBuffer.toString().trim().replaceAll(" +", " ");
                List<String> finalEffects = finalDescription.isEmpty()
                        ? new ArrayList<>() : List.of(finalDescription.split("\n"));

                System.out.println("Passive title: " + finalTitle);
                System.out.println("Cost: " + finalCost);
                System.out.println("Cost type: " + finalCostType);
                System.out.println("Description: " + finalDescription);

                this.resultPassive = new FormatedScraperData.Passive(finalTitle, finalCostType, costBuffer, finalEffects);

            }
        }
        public FormatedScraperData.Passive getBuiltPassive()
        {
            return this.resultPassive;
        }
    };

    public static class AbilityNodeVisitor implements NodeVisitor
    {
        // todo
    };
}
