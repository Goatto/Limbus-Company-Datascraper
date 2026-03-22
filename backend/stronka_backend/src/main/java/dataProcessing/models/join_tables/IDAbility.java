package dataProcessing.models.join_tables;

import dataProcessing.models.AbilityEntity;
import dataProcessing.models.IDEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "id_ability", schema = "public")
public class IDAbility
{
    @EmbeddedId
    private IDAbilityID id = new IDAbilityID();

    @ManyToOne
    @MapsId("idName")
    @JoinColumn(name = "id_name")
    private IDEntity idName;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "ability_id")
    private AbilityEntity ability;
}
