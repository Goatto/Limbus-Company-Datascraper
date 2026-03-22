package dataProcessing.models.join_tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AbilityStatusEffectsID
{

    @Column(name = "ability_id", nullable = false)
    private UUID id;

    @Column(name = "status_effect_icon", nullable = false)
    private String icon;

}
