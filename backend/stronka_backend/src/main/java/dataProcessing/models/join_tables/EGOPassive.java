package dataProcessing.models.join_tables;

import dataProcessing.models.EGOEntity;
import dataProcessing.models.PassiveEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ego_passive", schema = "public")
public class EGOPassive
{
    @EmbeddedId
    private EGOPassiveID id = new EGOPassiveID();

    @ManyToOne
    @MapsId("name")
    @JoinColumn(name = "ego_name")
    private EGOEntity ego;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "passive_id")
    private PassiveEntity passive;
}
