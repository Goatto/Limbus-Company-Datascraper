package dataProcessing;

import dataProcessing.webScraper.enums.Rarity;
import dataProcessing.webScraper.enums.ThreatLevel;

import java.util.*;

public class DTOBuilders
{

    public interface HasCombatPassive<T>
    {
        T addCombatPassive(UUID uuid);
    }

    public interface HasSupportPassive<T>
    {
        T addSupportPassive(UUID uuid);
    }
    public static abstract class BaseEquippableBuilder<T extends BaseEquippableBuilder<T>> implements HasCombatPassive<T>
    {
        protected String name;
        protected String sinnerName;
        protected String portraitFile;
        protected String season;
        protected String releaseDate;

        protected List<UUID> abilities = new ArrayList<>();
        protected List<UUID> combatPassives = new ArrayList<>();
        protected Map<String, Double> resistances = new HashMap<>();

        protected abstract T self();

        public T setName(String name)
        {
            this.name = name;
            return self();
        }

        public T setSinnerName(String sinnerName)
        {
            this.sinnerName = sinnerName;
            return self();
        }

        public T setPortraitFile(String portraitFile)
        {
            this.portraitFile = portraitFile;
            return self();
        }

        public T setSeason(String season)
        {
            this.season = season;
            return self();
        }

        public T setReleaseDate(String releaseDate)
        {
            this.releaseDate = releaseDate;
            return self();
        }

        public T addAbility(UUID ability)
        {
            this.abilities.add(ability);
            return self();
        }

        @Override
        public T addCombatPassive(UUID combatPassive)
        {
            this.combatPassives.add(combatPassive);
            return self();
        }

        public T addResistance(String damageType, Double multiplier)
        {
            this.resistances.put(damageType, multiplier);
            return self();
        }
    }
    public static class IDDataBuilder extends BaseEquippableBuilder<IDDataBuilder> implements HasSupportPassive<IDDataBuilder>
    {
        private Rarity rarity;
        private String world;
        private String worldFile;
        private int health;
        private String speed;
        private int defenseLevel;

        private final List<UUID> supportPassives = new ArrayList<>();;
        private final List<String> traits = new ArrayList<>();
        private final List<String> staggerThresholds = new ArrayList<>();
        private final List<String> positiveSanityEffects = new ArrayList<>();
        private final List<String> negativeSanityEffects = new ArrayList<>();

        // Zwracamy this żeby umożliwić chaining metod
        @Override
        protected IDDataBuilder self() {
            return this;
        }

        public IDDataBuilder setRarity(Rarity rarity)
        {
            this.rarity = rarity;
            return this;
        }

        public IDDataBuilder setWorld(String world)
        {
            this.world = world;
            return this;
        }

        public IDDataBuilder setWorldFile(String worldFile)
        {
            this.worldFile = worldFile;
            return this;
        }

        public IDDataBuilder setHealth(int health)
        {
            this.health = health;
            return this;
        }

        public IDDataBuilder setSpeed(String speed)
        {
            this.speed = speed;
            return this;
        }


        public IDDataBuilder setDefenseLevel(int defenseLevel)
        {
            this.defenseLevel = defenseLevel;
            return this;
        }

        @Override
        public IDDataBuilder addSupportPassive(UUID supportPassive)
        {
            this.supportPassives.add(supportPassive);
            return this;
        }

        public IDDataBuilder addTrait(String trait)
        {
            this.traits.add(trait);
            return this;
        }

        public IDDataBuilder addStaggerThreshold(String staggerThresholds)
        {
            this.staggerThresholds.add(staggerThresholds);
            return this;
        }

        public IDDataBuilder addPositiveSanityEffect(String positiveSanityLine)
        {
            this.positiveSanityEffects.add(positiveSanityLine);
            return this;
        }

        public IDDataBuilder addNegativeSanityEffect(String negativeSanityLine)
        {
            this.negativeSanityEffects.add(negativeSanityLine);
            return this;
        }

        public ScraperDataDTOs.IDData buildIDData()
        {
            return new ScraperDataDTOs.IDData(
                    name,sinnerName, portraitFile, rarity, world, worldFile, season, releaseDate,
                    health, speed, defenseLevel, supportPassives, traits,
                    staggerThresholds, resistances, positiveSanityEffects, negativeSanityEffects,
                    abilities, combatPassives
            );
        }
    }

    public static class EGODataBuilder extends BaseEquippableBuilder<EGODataBuilder>
    {
        private String corrodedPortraitFile;
        private ThreatLevel threatLevel;
        private String sinAffinity;
        private String abnormality;
        private int awakenSanityCost;
        private int corrosionSanityCost;

        private Map<String, Integer> awakenSinCost = new HashMap<>();
        private Map<String, Integer> corrosionSinCost = new HashMap<>();

        @Override
        protected EGODataBuilder self() {
            return this;
        }

        public EGODataBuilder setCorrededPortraitFile(String corrodedPortraitFile)
        {
            this.corrodedPortraitFile = corrodedPortraitFile;
            return this;
        }

        public EGODataBuilder setThreatLevel(ThreatLevel threatLevel)
        {
            this.threatLevel = threatLevel;
            return this;
        }

        public EGODataBuilder setSinAffinity(String sinAffinity)
        {
            this.sinAffinity = sinAffinity;
            return this;
        }

        public EGODataBuilder setAbnormality(String abnormality)
        {
            this.abnormality = abnormality;
            return this;
        }

        public EGODataBuilder setAwakenSanityCost(int awakenSanityCost)
        {
            this.awakenSanityCost = awakenSanityCost;
            return this;
        }

        public EGODataBuilder setCorrosionSanityCost(int corrosionSanityCost)
        {
            this.corrosionSanityCost = corrosionSanityCost;
            return this;
        }

        public EGODataBuilder setAwakenSinCost(Map<String, Integer> awakenSinCost)
        {
            this.awakenSinCost = awakenSinCost;
            return this;
        }

        public EGODataBuilder setCorrosionSinCost(Map<String, Integer> corrosionSinCost)
        {
            this.corrosionSinCost = corrosionSinCost;
            return this;
        }

        public ScraperDataDTOs.EGOData buildEGOData()
        {
            return new ScraperDataDTOs.EGOData(
                    name, sinnerName,  portraitFile, corrodedPortraitFile, threatLevel, season, releaseDate, sinAffinity, abnormality,
                    awakenSanityCost, corrosionSanityCost, resistances, awakenSinCost, corrosionSinCost,
                    abilities, combatPassives
            );
        }
    }

    public static class AbilityDataBuilder
    {
        private String skillSlot;
        private String abilityName;
        private String sinAffinity;
        private String skillIconFile;
        private String attackWeight;
        private int basePower;
        private String damageType;
        private String coinPower;
        private int coinCount;
        private String offenseLevel;

        private List<String> baseEffects = new ArrayList<>();
        private Map<String, List<String>> coinEffects = new HashMap<>();
        private Set<String> statusEffects = new HashSet<>();

        public AbilityDataBuilder setSkillSlot(String skillSlot)
        {
            this.skillSlot = skillSlot;
            return this;
        }

        public AbilityDataBuilder setAbilityName(String abilityName)
        {
            this.abilityName = abilityName;
            return this;
        }

        public AbilityDataBuilder setSinAffinity(String sinAffinity)
        {
            this.sinAffinity = sinAffinity;
            return this;
        }

        public AbilityDataBuilder setSkillIconFile(String skillIconFile)
        {
            this.skillIconFile = skillIconFile;
            return this;
        }

        public AbilityDataBuilder setAttackWeight(String attackWeight)
        {
            this.attackWeight = attackWeight;
            return this;
        }

        public AbilityDataBuilder setBasePower(int basePower)
        {
            this.basePower = basePower;
            return this;
        }

        public AbilityDataBuilder setDamageType(String damageType)
        {
            this.damageType = damageType;
            return this;
        }

        public AbilityDataBuilder setCoinPower(String coinPower)
        {
            this.coinPower = coinPower;
            return this;
        }

        public AbilityDataBuilder setCoinCount(int coinCount)
        {
            this.coinCount = coinCount;
            return this;
        }

        public AbilityDataBuilder setOffenseLevel(String offenseLevel)
        {
            this.offenseLevel = offenseLevel;
            return this;
        }

        public AbilityDataBuilder setBaseEffects(List<String> baseEffects)
        {
            this.baseEffects = baseEffects;
            return this;
        }

        public AbilityDataBuilder setCoinEffects(Map<String, List<String>> coinEffects)
        {
            this.coinEffects = coinEffects;
            return this;
        }

        public AbilityDataBuilder setStatusEffects(Set<String> statusEffect)
        {
            this.statusEffects = statusEffect;
            return this;
        }

        public ScraperDataDTOs.Ability buildAbilityData()
        {
            return new ScraperDataDTOs.Ability(
                    skillSlot, abilityName, sinAffinity, skillIconFile, attackWeight, basePower,
                    damageType, coinPower, coinCount, offenseLevel, baseEffects, coinEffects, statusEffects
            );
        }
    }
}
