package dataProcessing.webScraper.utils;

import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImageScraper
{
    static Map<String, String> httpHeaders = Map.of(
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

    // Lista plików, przez które już przeszliśmy, i nie chcemy pobierać ponownie
    private static final Set<String> checkedFiles = new HashSet<>();
    private static final String storagePath = "data/images/";
    // Pusty klient do sprawdzania statusu
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Metoda odpowiednia za zebranie URL do obrazu z danego Elementu, jak i przekazaniem tej wartości do {@link #downloadScrapedImageURL(String, String)}
     * która jest odpowiedzialna za pobranie obrazu na komputer.
     * @param imgElement Element, który zawiera obrazek, którego URL chcemy zebrać.
     * @return Nazwę pliku obrazka, który właśnie został zapisany na komputerze.
     */
    // TODO Zmodyfikować tak by zamienić _ na spacje, jak i zamienić znaki na faktyczne ich wartości
    public static String scrapeImageURL(Element imgElement)
    {
        // Upewniamy się, że nie otrzymujemy nulla
        if(imgElement == null)
        {
            System.out.println("VALUE PASSED IN scrapeImageURL IS NULL!!!!");
            return null;
        }

        String imgURL = imgElement.attr("abs:src");

        // Pobieranie obrazów w pełnej rozdzielczości
        if (imgURL.contains("/thumb/")) {
            imgURL = imgURL.replace("/thumb/", "/");
            imgURL = imgURL.substring(0, imgURL.lastIndexOf("/"));
        }

        String fileName = imgElement.attr("alt");
        fileName = fileName .replaceAll("[<>:\"/\\\\|?*]", "");
        // TODO Odkomentować jeżeli pojawią się jakieś problemy
        // fileName = fileName.replaceAll("\\d+-", "");
        // Znaki po oraz samo ? uniemożliwiają pobieranie, więc się ich pozbywamy
        if (fileName.contains("?"))
        {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }

        // Jeżeli w liście sprawdzonych plików mamy już obecny plik, przerywamy
        if(checkedFiles.contains(fileName))
        {
            return null;
        }

        File downloadedFile = new File(storagePath + fileName);
        // Jeżeli w pobranych plikach mamy już obecny plik, dodajemy go do listy sprawdzonych i przerywamy
        if(downloadedFile.exists())
        {
            checkedFiles.add(fileName);
            return null;
        }

        // add zwraca true, tylko jeśli doszło do dodania nowego elementu
        if(!imgURL.isEmpty() && checkedFiles.add(imgURL))
        {
            System.out.println("Registered new image: " + imgURL);
            downloadScrapedImageURL(imgURL, fileName);
        }
        return fileName;
    }

    /**
     * Metoda prywatna odpowiednia za pobranie pliku w {@link #scrapeImageURL(Element)}.
     * @param imgURL URL do pliku, który chcemy pobrać.
     * @param fileName Nazwa, pod którą chcemy zapisać pobrany plik.
     */
    private static void  downloadScrapedImageURL(String imgURL, String fileName)
    {
        try
        {
            // Sprawdzamy, czy folder istnieje, jeżeli nie to go tworzymy
            Path directory = Paths.get(storagePath);
            if(Files.notExists(directory))
            {
                Files.createDirectories(directory);
            }
            // Łączy ścieżkę folderu do zapisu wraz z nazwą pliku, by otrzymać pełną ścieżke do zapisu
            // Ta metoda, działa pomiędzy różnymi systemami, więc nie musimy się martwić czy mamy dać /, czy \
            Path targetPath = directory.resolve(fileName);

            // Wykorzystujemy wbudowanego buildera, by stworzyć prosty request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    // Zamienia tekst z imgURL na obiekt, z którego faktycznie operujemy nad zasobem
                    .uri(URI.create(imgURL))
                    // Mówimy o tym, że chcemy pobrać zasób
                    .GET();

            // Dodajemy header do naszego requesta
            httpHeaders.forEach(requestBuilder::header);
            // I w końcu budujemy cały request
            HttpRequest request = requestBuilder.build();
            // Sprawdzamy, czy możemy się połączyć
            HttpResponse<InputStream> httpResponse = httpClient.send(request,
                    // Nie 'dotykamy' pliku, sprawdzamy tylko nagłówki i.e. czy istnieje
                    HttpResponse.BodyHandlers.ofInputStream());
            // Kod 200 to 'Ok' oznacza to, że udało nam się przejść przez zasób
            if(httpResponse.statusCode() == 200)
            {
                Files.copy(httpResponse.body(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Successfully downloaded: " + fileName);
            }
            else
            {
                System.out.println("HTTP Error: " + httpResponse.statusCode());
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
