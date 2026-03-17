package dataProcessing.repositories;

import dataProcessing.models.StatusEffectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
    Interfejsy, które posiadają adnotację @Repository, korzystają z mechanizmu Dynamic Proxy.
    Spring, przy uruchomieniu aplikacji, szuka właśnie ów interfejsów, po czym generuje ukrytą klasę,
    która ten interfejs implementuje. Jest to właśnie mechanizm Dynamic Proxy.
    Wewnątrz wygenerowanej klasy Spring generuje nam kod, który otwiera połączenie z bazą PostgreSQL,
    jak i generuje odpowiedni kod SQL. Po wykonaniu tego kodu zamienia wyniki na nasze obiekty
 */
@Repository
// Pierwszy argument JpaRepository to nasza klasa encji
// Drugi argument to typ naszego klucza głównego
public interface StatusEffectRepository extends JpaRepository<StatusEffectEntity, String>
{
    /*
        Rozrzeszając JpaRepository, dziedziczymy wiele gotowych metod, między innymi:
            .save(entity) - Spring generuje INSERT lub UPDATE
            .findById("foo") - Spring generuje select dla naszego klucza głównego z parametrem foo
            .findAll() - Spring pobiera wszystkie wiersze tablicy jako listę
            // TODO To później akurat możesz wykorzystać by parser niepotrzebnie nie zbierał 1000 razy tych samych danych
            .existsById() - Zwraca true lub false, zależnie od tego czy rekord istnieje czy nie
            .deleteById("foo") - Spring generuje zapytanie DELETE
    */
    /*
        Repozytoria implementują też czarną magię zwaną 'Derived Query Methods', które automatycznie generuje
        zapytania SQL, zależnie od nazwy metody i jej typu, struktura to:
        [co chcemy zrobić] + By + [po jakich polach mamy szukać] + (opcjonalne warunki)
        gdzie:
            [co chcemy zrobić] - Prefix metody, który jest jednym ze znanych Springowi słów, między innymi są to:
                find/read - Pobierz dane, zwraca obiekt lub listę obiektów
                exists - Sprawdź czy istnieje, zwraca boolean (bardzo szybkie!)
                count - Liczy ile jest, zwraca liczbę long
                delete - Usuń, zwykle wykonuje operację DELETE, zwraca ilość usuniętych rekordów
            By + [po jakich polach mamy szukać] - Środkowa porcja metody, odpowiednia za określenie dokładnej nazwy pola,
            które znajduje się w naszej klasie, nazwy muszą być TAKIE SAME, gdyż Spring sprawdza naszą klasę i dokonuje
            dopasowań:
                Przykładowo, w klasie którą obsługuje te repozytorium, mamy polę String o nazwie icon,
                więc możemy napisać: 'findByIcon(String icon)'
            (opcjonalne warunki) - Suffixy naszej metody, pozwalają na łączenie jak i modyfikowanie naszej metody
            między innymi są to: And/Or, LessThan, Containing, IgnoreCase

         NIESTETY! Nie są one perfekcyjnie, kompletnie zagubią sie na operowaniu nad kolumnami JSONB
         Więc takie rzeczy trzeba samemu obsłużyć
     */

}

