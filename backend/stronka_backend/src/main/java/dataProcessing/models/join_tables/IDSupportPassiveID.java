package dataProcessing.models.join_tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IDSupportPassiveID
{
    @Column(name = "id_name", nullable = false)
    private String idName;

    @Column(name = "support_passive_id", nullable = false)
    private UUID id;
}

