package dataProcessing.models.join_tables;

import dataProcessing.models.AbilityEntity;
import dataProcessing.models.EGOEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ego_ability", schema = "public")
public class EGOAbility
{
    @EmbeddedId
    private EGOAbilityID id = new EGOAbilityID();

    @ManyToOne
    @MapsId("name")
    @JoinColumn(name = "ego_name")
    private EGOEntity ego;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "ability_id")
    private AbilityEntity ability;
}
