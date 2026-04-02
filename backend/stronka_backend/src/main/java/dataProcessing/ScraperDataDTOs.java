package dataProcessing;

import dataProcessing.webScraper.enums.Tiers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScraperDataDTOs
{

    /**
     * Rekord głowny odpowiedni za zebranie wszystkich EGO oraz ID do jednego sinner'a
     * @param sinner Nazwa sinner'a
     * @param IDs Lista jego ID
     * @param EGOs Lista jego EGO
     */
    public record SinnerData(
            String sinner,
            List<IDData> IDs,
            List<EGOData> EGOs
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednym ID
     * @param name Nazwa ID
     // * @param sinnerName Nazwa sinnera, który posiada te ID
     * @param portraitFile Nazwa pliku zawierająca portret ID
     * @param rarity Rzadkość ID
     * @param world Świat, do którego należy ID
     * @param worldFile Plik zawierający obraz świata
     * @param season Sezon wydania ID
     * @param releaseDate Data wydania ID
     * @param health Maksymalne życie ID
     * @param speed Przedział prędkości ID
     * @param defenseLevel Maksymalny defenseLevel jednostki
     * @param supportPassives Umiejętność pasywna wspierająca jednostki
     * @param traits Wszystkie cechy jednostki
     * @param staggerThresholds Wszystkie stagger thresholdy jednostki
     * @param resistances Wszystkie stopnie odporności na obrażenia jednostki
     * @param positiveSanityEffects Pozytywne efekty sanity jednostki
     * @param negativeSanityEffects Negatywne efekty sanity jednostki
     * @param abilities Wszystkie umiejętności jednostki
     * @param combatPassives Wszystkie umiejętności pasywne jednostki wykorzystywane w walce
     */
    // Nie korzystamy z interfejsu, z racji, że utrudni mi to parsowanie danych do DB
    public record IDData(
            String name,
            String sinnerName,
            String portraitFile,
            Tiers.Rarity rarity,
            String world,
            String worldFile,
            String season,
            String releaseDate,
            int health,
            String speed,
            int defenseLevel,
            List<UUID> supportPassives,

            List<String> traits,
            List<String> staggerThresholds,
            Map<String, Double> resistances,
            // Ewentualnie później można oddzielić te dwie do osobnego typu danych
            List<String> positiveSanityEffects,
            List<String> negativeSanityEffects,
            List<UUID> abilities,
            List<UUID> combatPassives
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednym EGO
     * @param name Nazwa Ego
     // * @param sinnerName Nazwa sinnera, który posiada te EGO
     * @param portraitFile Nazwa pliku zawierająca portret EGO
     * @param corrodedPortraitFile
     * @param threatLevel Poziom zagrożenia EGO (ie. w jakim slot'cie jest wykorzystany w grze)
     * @param season Sezon wydania ID
     * @param releaseDate Data wydania ID
     * @param sinAffinity Główny sin EGO
     * @param abnormality Abnormality, od którego pochodzi EGO
     * @param awakenSanityCost Zwykły koszt sanity wywołania EGO
     * @param corrosionSanityCost Koszt sanity wywołania EGO przy korozji
     * @param resistances Wszystkie stopnie odporności na obrażenia jednostki
     * @param awakenSinCost Zwykły koszt sin'ów wywołania EGO
     * @param corrosionSinCost Koszt sin'ów wywołania EGO przy korozji
     * @param abilities Wszystkie umiejętności jednostki
     * @param combatPassives Wszystkie umiejętności pasywne jednostki wykorzystywane w walce
     */
    public record EGOData(
        String name,
        // TODO sprawdzić czy to ma sens
        String sinnerName,
        String portraitFile,
        String corrodedPortraitFile,
        Tiers.ThreatLevel threatLevel,
        String season,
        String releaseDate,
        String sinAffinity,
        String abnormality,
        int awakenSanityCost,
        int corrosionSanityCost,

        Map<String, Double> resistances,
        Map<String, Integer> awakenSinCost,
        Map<String, Integer> corrosionSinCost,
        List<UUID> abilities,
        List<UUID> combatPassives


    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednej umiejętności, ID czy to EGO
     * @param skillSlot Stopien umiejętności
     * @param abilityName Nazwa umiejętności
     * @param sinAffinity Główny sin umiejętności
     * @param skillIconFile Nazwa pliku zawierająca ikonke umiejętności
     * @param attackWeight Attack weight umiejętności (ie. ilu przeciwników trafia na raz)
     * @param basePower Podstawowa siła umiejętności
     * @param damageType Typ obrażeń umiejętności
     * @param coinPower Dodana siła umiejętności za każdą monete
     * @param coinCount Ilość monet w umiejętności
     * @param offenseLevel Ile dodatkowego offenseLevel ma umiejętność
     * @param baseEffects Efekty umiejętności wywoływane przed rzuceniem pierwszej monety
     * @param coinEffects Efekty umiejętności dla danej monety
     */
    public record Ability(
            String skillSlot,
            String abilityName,
            String sinAffinity,
            String skillIconFile,
            String attackWeight,
            int basePower,
            String damageType,
            String coinPower,
            int coinCount,
            String offenseLevel,

            List<String> baseEffects,
            Map<String, List<String>> coinEffects,
            Set<String> statusEffects
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednej umiejętności pasywnej, combat czy to support
     *
     * @param passiveName         Nazwa umiejętności pasywnej
     * @param costType            Jaki jest typ jej kosztu (ie. resonance czy owned, jeżeli ma koszt)
     * @param cost                Ilość sin'ów wymaganych do spełnienia kosztu
     * @param description         Opis umiejętności pasywnej (i.e. co ona robi)
     */
    public record Passive(
            String passiveName,
            String costType,
            Map<String, Integer> cost,
            List<String> description,
            Set<String> statusEffects
            ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednym status-effect'cie
     * @param name Nazwa status-effect'u
     * @param icon Nazwa pliku zawierająca ikonke status-effect'u
     * @param description Opis status-effect'u (i.e. co on robi)
     */
    public record StatusEffect(
            String name,
            String icon,
            List<String> description
    ){}

    // TODO ewentualnie też później pomyśl o stworzeniem rekordu/encji tylko dla sanity effectów,
    //  z racji że bardzo często się potwarzają
}
