package dataProcessing.models.join_tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SinnerIDID
{
    @Column(name = "sinner", nullable = false)
    private String sinner;

    @Column(name = "id_name", nullable = false)
    private String idName;
}
