package dataProcessing.models.join_tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EGOPassiveID
{
    @Column(name = "ego_name", nullable = false)
    private String name;

    @Column(name = "passive_id", nullable = false)
    private UUID id;
}
