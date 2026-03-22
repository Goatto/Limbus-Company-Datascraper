package dataProcessing.models.join_tables;

import dataProcessing.models.IDEntity;
import dataProcessing.models.PassiveEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "id_passive", schema = "public")
public class IDPassive
{
    @EmbeddedId
    private IDPassiveID id = new IDPassiveID();

    @ManyToOne
    @MapsId("idName")
    @JoinColumn(name = "id_name")
    private IDEntity idName;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "passive_id")
    private PassiveEntity passive;
}
