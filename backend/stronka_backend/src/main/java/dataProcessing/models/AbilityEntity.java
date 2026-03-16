package dataProcessing.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "abilities")
public class AbilityEntity
{
    // Niestety nie jesteśmy pewni, że dwie pasywki nie mają takiej samej nazwy
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "skill_slot", nullable = false)
    private String skillSlot;

    @Column(name = "name", nullable = false)
    private String abilityName;

    @Column(name = "sin_affinity", nullable = true)
    private String sinAffinity;

    @Column(name = "icon", nullable = false)
    private String skillIconFile;

    @Column(name = "attack_weight", nullable = false)
    private String attackWeight;

    @Column(name = "base_power", nullable = false)
    private int basePower;

    @Column(name = "damage_type", nullable = false)
    private String damageType;

    @Column(name = "coin_power", nullable = false)
    private String coinPower;

    @Column(name = "coin_count", nullable = false)
    private int coinCount;

    @Column(name = "offense_level", nullable = false)
    private String offenseLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_effects", nullable = false)
    private List<String> baseEffects = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coin_effects", nullable = false)
    private Map<String, List<String>> coinEffects = new HashMap<>();

    // Tu akurat mogę zrobić ManyToMany z racji, że wiele umiejętności może mieć wiele status-effect'ów
    @ManyToMany
    // Oznaczenie tworzące trzecią tabelę pośrednią
    @JoinTable(
            // Nazwa naszej nowej tabeli
            name ="ability_status-effects",
            // Kolumna wskazująca na nas, np. nasze ID
            joinColumns = @JoinColumn(name = "ability_id"),
            // Kolumna wskazująca na ID encji, która zbiera 'nas'
            inverseJoinColumns = @JoinColumn(name = "status_effect_id")
    )
    private List<StatusEffectEntity> statusEffects = new ArrayList<>();

    // @ManyToOne to odzwierciedlenie kolumny w bazie
    // FetchType.Lazy to parametr fetch, który oznacza, że dane będą pobrane tylko, gdy je wywołamy, np. poprzez foo.getBar
    @ManyToOne(fetch = FetchType.LAZY)
    // JoinColumn określa nazwę kolumny z kluczem obcym
    @JoinColumn(name = "ego_name")
    private EGOEntity ego;
}
