package dataProcessing.models;

import dataProcessing.models.join_tables.PassiveStatusEffects;
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
@Table(name = "passives")
public class PassiveEntity
{
    // Niestety nie jesteśmy pewni, że dwie pasywki nie mają takiej samej nazwy
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cost_type", nullable = false)
    private String costType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cost", nullable = false)
    private Map<String, Integer> cost = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", nullable = false)
    private List<String> description = new ArrayList<>();

    // Tu akurat mogę zrobić ManyToMany z racji, że wiele pasywek może mieć wiele status-effect'ów
    @OneToMany(mappedBy = "passive")
    private List<PassiveStatusEffects> statusEffects = new ArrayList<>();
}
