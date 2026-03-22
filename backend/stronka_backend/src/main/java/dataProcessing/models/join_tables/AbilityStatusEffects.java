package dataProcessing.models.join_tables;

import dataProcessing.models.AbilityEntity;
import dataProcessing.models.StatusEffectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ability_status_effects", schema = "public")
public class AbilityStatusEffects
{
    @EmbeddedId
    private AbilityStatusEffectsID id = new AbilityStatusEffectsID();

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "ability_id")
    private AbilityEntity ability;

    @ManyToOne
    @MapsId("icon")
    @JoinColumn(name = "status_effect_icon")
    private StatusEffectEntity statusEffect;
}
