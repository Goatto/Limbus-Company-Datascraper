package dataProcessing.webScraper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormatedScraperData
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
     * @param portraitFile Nazwa pliku zawierająca portret ID
     * @param rarity Rzadkość ID
     * @param world Świat, do którego należy ID
     * @param worldFile Plik zawierający obraz świata
     * @param season Sezon wydania ID
     * @param releaseDate Data wydania ID
     * @param health Maksymalne życie ID
     * @param speed Przedział prędkości ID
     * @param defenseLevel Maksymalny defenseLevel jednostki
     * @param supportPassive Umiejętność pasywna wspierająca jednostki
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
        String portraitFile,
        Tiers.Rarity rarity,
        String world,
        String worldFile,
        String season,
        String releaseDate,
        int health,
        String speed,
        int defenseLevel,
        Passive supportPassive,

        List<String> traits,
        List<String> staggerThresholds,
        Map<String, Double> resistances,
        // Ewentualnie później można oddzielić te dwie do osobnego typu danych
        List<String> positiveSanityEffects,
        List<String> negativeSanityEffects,
        List<Ability> abilities,
        List<Passive> combatPassives
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednym EGO
     * @param name Nazwa Ego
     * @param portraitFile Nazwa pliku zawierająca portret EGO
     * @param threatLevel Poziom zagrożenia EGO (ie. w jakim slotcie jest wykorzystany w grze)
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
        String portraitFile,
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
        List<Ability> abilities,
        List<Passive> combatPassives


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
            // TODO ale jaki skillSlot ma ego?
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
            Map<String, List<String>> coinEffects
            // Set<StatusEffect> statusEffects
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednej umiejętności pasywnej, combat czy to support
     * @param passiveName Nazwa umiejętności pasywnej
     * @param costType Jaki jest typ jej kosztu (ie. resonance czy owned, jeżeli ma koszt)
     * @param cost Ilość sin'ów wymaganych do spełnienia kosztu
     * @param description Opis umiejętności pasywnej (i.e. co ona robi)
     */
    public record Passive(
            String passiveName,
            String costType,
            Map<String, Integer> cost,
            List<String> description
            // Set<StatusEffect> statusEffects
    ){}

    /**
     * Rekord odpowiedni za zebranie wszystkich informacji o jednym status-effect'cie
     * @param name Nazwa status-effect'u
     * @param icon Nazwa pliku zawierająca ikonke status-effect'u
     * @param description Opis status-effect'u (i.e. co on robi)
     * @param relatedEffects Nazwy innych status-effect'ów znajdujących się w opisie
     */
    public record StatusEffect(
            String name,
            String icon,
            List<String> description,
            Set<String> relatedEffects
    ){}
}
