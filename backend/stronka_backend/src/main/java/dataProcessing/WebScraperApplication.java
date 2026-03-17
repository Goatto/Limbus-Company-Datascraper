package dataProcessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
    Co to Bean w Springu?
    - Bean to obiekt zarządzany pzez Springa
    - W Springu każdy serwis czy inna klasa jest konstruowana automatycznie i wstrzykiwana
    zależnie od kontekstu
    - Sama klasa nie jest beanem, tylko obiekt klasy, która ma adnotacje @Component, która sama
    jest 'rodzicem' (lecz nadal możemy wszędzie korzystać z adnotacji @Component, zwykle nasz kod będzie
    mniej czytelny) dla innych bardziej specyficznych adnotacji:
        x @Service - Adnotacja odpowiednia za oznaczenie klasy jako pewnego typu pośrednika pomiędzy
        @Controller a @Repository, klasy oznaczone @Service są odpowiednie za przetworzenie prośby danej im
        przez @Controller, przy pomocy @Repository. @Service opisuje jak wykonać zadania, które mogą zostać
        odebrane przez @Controller
        x @Repository - Adnotacja odpowiednia za oznaczenie klasy jako DAO (Data Access Object),
        czyli klas odpowiedzialnych za komuniakcję z bazą danych
        x @Controller/@RestController - Adnotacja odpowiednia za oznaczenie klasy jako 'komunikatora' pomiędzy
        frontendem a backendem, zbiera zapytanie od frontu, i wie, do którego @Service musi pójśc, by otrzymać i
        przekazać odpowiedź
            y @Controller - Zwraca 'widok' np. plik HTML
            y @RestController - Zwraca dane, zazwyczaj JSON czy XML
 */

/*
    Adnotacja @SpringBootApplication to odpowiednik trzech innych adnotacji:
    - @EnableAutoConfiguration - Aktywuje automatyczną konfigurację aplikacji Spring na podstawie
    zależności jar
    - @ComponentScan - Włącza skanowanie na poziomie pakietu za klasami z adnotacją @Component
    - @Configuration - Oznacza obiekt jako źródło definicji Bean'ów (metod oznaczonych przez @Bean)
 */
@SpringBootApplication
public class WebScraperApplication
{
    static void main(String[] args)
    {
        SpringApplication.run(WebScraperApplication.class, args);
    }
}
