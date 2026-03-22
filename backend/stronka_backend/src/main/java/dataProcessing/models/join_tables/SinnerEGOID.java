package dataProcessing.models.join_tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SinnerEGOID
{
    @Column(name = "sinner", nullable = false)
    private String sinner;

    @Column(name = "ego_name", nullable = false)
    private String name;
}
