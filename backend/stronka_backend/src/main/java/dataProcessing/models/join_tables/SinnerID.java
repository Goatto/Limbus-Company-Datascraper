package dataProcessing.models.join_tables;

import dataProcessing.models.IDEntity;
import dataProcessing.models.SinnerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sinner_id", schema = "public")
public class SinnerID
{
    @EmbeddedId
    private SinnerIDID id = new SinnerIDID();

    @ManyToOne
    @MapsId("sinner")
    @JoinColumn(name = "sinner")
    private SinnerEntity sinner;

    @ManyToOne
    @MapsId("idName")
    @JoinColumn(name = "id_name")
    private IDEntity idName;

}
