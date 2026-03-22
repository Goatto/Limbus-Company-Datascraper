package dataProcessing.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.util.List;
import java.util.Set;

// Adnotacje Lomboka, automatycznie generujące:
// Gettery
@Getter
// Settery
@Setter
// Puste konstruktory, wymagane przez hibernate
@NoArgsConstructor
/*
    Dlaczego hibernate potrzebuje pustych konstruktorów?
    1. Hibernate wysyła zapytanie do bazy danych, która zwraca surowe dane tekstowe
    2. Hibernate ma teraz surowe dane, lecz musi je oddać jako dany obiekt.
       Hibernate nie korzysta z naszych już istniejących konstruktorów, lecz korzysta z Java Reflaction API,
       które pozwala mu na dynamicznie tworzenie obiektów podczas działania programu.
       Robi to poprzez użycie pustego konstruktora, które tworzy pusty obiekt
    3. Hibernate teraz przechodzi do wypełnienia pustego obiektu danymi, poprzez wykorzystanie setterów
    4. Po włożeniu wszystkich danych do obiektu Hibernate zwraca nam wypełniony obiekt
 */

// Adnotacja odpowiednia za określenie klasy jako encji bazy danych
@Entity
// Oznaczenie klasy jako tabelki w bazie danych
@Table(name = "status_effects", schema = "public")
public class StatusEffectEntity
{
    // Klucz główny
    @Id
    // Nazwa kolumny, jak i jej parametry
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon", nullable = false)
    private String icon;

    // Adnotacja @JdbcTypeCode(SqlTypes.JSON), wraz z parametrem columnDefinition = "jsonb" w definicji kolumny
    // Pozwalają na zamienienie naszych kolekcji na typ JSONB w PostreSQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    /*
        TODO co to jsonb?
     */
    private List<String> description;
}
