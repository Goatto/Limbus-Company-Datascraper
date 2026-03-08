package dataProcessing.webScraper;

import java.util.List;
import java.util.Map;

//TODO Zamienić rarity i threat level na enumy
public class FormatedScraperData {

    public record SinnerData(
            String sinner,
            List<IDData> IDs,
            List<EGOData> EGOs
    ){}

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
        List<String> positiveSanityEffects,
        List<String> negativeSanityEffects,
        List<Ability> abilities,
        List<Passive> combatPassives
    ){}

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
            Map<String, List<String>> coinEffects
    ){}

    public record Passive(
            String passiveName,
            String costType,
            Map<String, Integer> cost,
            List<String> effects
    ){}
}
