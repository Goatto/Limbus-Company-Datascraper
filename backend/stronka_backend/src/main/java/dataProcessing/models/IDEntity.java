package dataProcessing.models;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.join_tables.IDAbility;
import dataProcessing.models.join_tables.IDPassive;
import dataProcessing.webScraper.enums.Tiers;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "identity", schema = "public")
public class IDEntity
{
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sinner", nullable = false)
    private String sinner;

    @Column(name = "portrait_file", nullable = false)
    private String portraitFile;

    @Column(name = "tier", nullable = false)
    private Tiers.Rarity rarity;

    @Column(name = "world", nullable = false)
    private String world;

    @Column(name = "world_icon", nullable = false)
    private String worldFile;

    @Column(name = "season", nullable = false)
    private String season;

    @Column(name = "release_date", nullable = false)
    private String releaseDate;

    @Column(name = "health", nullable = false)
    private int health;

    @Column(name = "speed", nullable = false)
    private String speed;

    @Column(name = "defense_level", nullable = false)
    private int defenseLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "traits")
    private List<String> traits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stagger_thresholds")
    private List<String> staggerThresholds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resistances")
    private Map<String, Double> resistances;

    // Ewentualnie później można oddzielić te dwie do osobnego typu danych
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "positive_sanity_effects")
    private List<String> positiveSanityEffects = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "negative_sanity_effects")
    private List<String> negativeSanityEffects = new ArrayList<>();

    @OneToMany(mappedBy = "idName", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IDAbility> abilities = new ArrayList<>();

    @OneToMany(mappedBy = "idName", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IDPassive> combatPassive = new ArrayList<>();

    @Column(name = "support_passive")
    private UUID supportPassive;

    // Tutaj będzie nawiązanie do sinnera
    // private String sinnerName;
}
