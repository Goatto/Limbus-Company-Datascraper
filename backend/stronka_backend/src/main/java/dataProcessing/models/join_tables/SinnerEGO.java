package dataProcessing.models.join_tables;

import dataProcessing.models.EGOEntity;
import dataProcessing.models.SinnerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sinner_ego", schema = "public")
public class SinnerEGO
{
    @EmbeddedId
    private SinnerEGOID id = new SinnerEGOID();

    @ManyToOne
    @MapsId("sinner")
    @JoinColumn(name = "sinner")
    private SinnerEntity sinner;

    @ManyToOne
    @MapsId("name")
    @JoinColumn(name = "ego_name")
    private EGOEntity ego;
}
