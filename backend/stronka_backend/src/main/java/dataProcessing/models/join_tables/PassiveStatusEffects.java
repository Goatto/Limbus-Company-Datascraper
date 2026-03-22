package dataProcessing.models.join_tables;

import dataProcessing.models.PassiveEntity;
import dataProcessing.models.StatusEffectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "passive_status_effects", schema = "public")
public class PassiveStatusEffects
{
    @EmbeddedId
    private PassiveStatusEffectsID id = new PassiveStatusEffectsID();

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "passive_id")
    private PassiveEntity passive;

    @ManyToOne
    @MapsId("icon")
    @JoinColumn(name = "status_effect_icon")
    private StatusEffectEntity statusEffect;
}
