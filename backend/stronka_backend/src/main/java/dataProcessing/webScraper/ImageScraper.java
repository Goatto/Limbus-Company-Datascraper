package dataProcessing.webScraper;

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
import java.util.Optional;
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

    private static final Set<String> checkedFiles = new HashSet<>();
    private static final String storagePath = "data/images";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void scrapeImageURL(Element imgElement)
    {
        if(imgElement == null)
        {
            System.out.println("VALUE PASSED IN 'scrapeImageURL' IS NULL!!!!");
        }

        String imgURL = Optional.ofNullable(imgElement)
                .map(attribute -> attribute.attr("abs:src"))
                .orElse("NO IMAGE URL!!!");
        String fileName = imgURL.substring(imgURL.lastIndexOf('/') + 1);
        // Znaki po, jak i ? nie są konieczne, jak i uniemożliwiają pobieranie
        if (fileName.contains("?"))
        {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }

        if(checkedFiles.contains(fileName))
        {
            return;
        }
        File downloadedFile = new File(storagePath + fileName);
        if(downloadedFile.exists())
        {
            checkedFiles.add(fileName);
            return;
        }
        // add zwraca true, tylko jeśli doszło do dodania nowego elementu
        if(!imgURL.isEmpty() && checkedFiles.add(imgURL))
        {
            System.out.println("Registered new image: " + imgURL);
            downloadScrapedImageURL(imgURL, fileName);
        }
    }

    // TODO Jeżeli mam być szczery to do końca nie wiem jak wszystko tu działa, zwyklę sprawdziłem na internecie jak się to robi
    // TODO Więc się muszę tego ewentualnie nauczyć
    private static void downloadScrapedImageURL(String imgURL, String fileName)
    {
        // Czy folder istnieje?
        try
        {
            Path directory = Paths.get(storagePath);
            if(Files.notExists(directory))
            {
                Files.createDirectories(directory);
            }
            Path targetPath = directory.resolve(fileName);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(imgURL))
                    .GET();

            httpHeaders.forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();

            HttpResponse<InputStream> httpResponse = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());
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
