package dataProcessing.models;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.webScraper.enums.Tiers;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sinner_egos", schema = "public")
public class EGOEntity
{
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "portrait_file", nullable = true)
    private String portraitFile;

    @Column(name = "corroded_portrait_file", nullable = true)
    private String corrodedPortraitFile;

    @Column(name = "tier", nullable = false)
    private Tiers.ThreatLevel threatLevel;

    @Column(name = "season", nullable = false)
    private String season;

    @Column(name = "release_date", nullable = false)
    private String releaseDate;

    @Column(name = "sin_affinity", nullable = false)
    private String sinAffinity;

    @Column(name = "abnormality", nullable = true)
    private String abnormality;

    @Column(name = "awakening_sanity_cost", nullable = false)
    private int awakenSanityCost;

    @Column(name = "corrosion_sannity_cost", nullable = true)
    private int corrosionSanityCost;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resistances", nullable = false)
    private Map<String, Double> resistances = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "awakening_sin_cost", nullable = false)
    private Map<String, Integer> awakenSinCost = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "corrosion_sin_cost", nullable = true)
    private Map<String, Integer> corrosionSinCost = new HashMap<>();

    // Wirtualny widok wskazujący na wszystkie klucze obce wskazujące na nas
    @OneToMany(mappedBy = "ego", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbilityEntity> abilities = new ArrayList<>();

    /*
    @OneToMany(mappedBy = "passive")
    @Column(name = "passives", nullable = true)
    private List<PassiveEntity> combatPassive = new ArrayList<>();
    */

    public void addAbility(AbilityEntity ability)
    {
        abilities.add(ability);
        ability.setEgo(this);
    }

    public void removeAbility(AbilityEntity ability)
    {
        abilities.remove(ability);
        ability.setEgo(null);
    }
}

