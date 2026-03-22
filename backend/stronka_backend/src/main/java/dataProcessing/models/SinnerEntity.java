package dataProcessing.models;

import dataProcessing.models.join_tables.SinnerEGO;
import dataProcessing.models.join_tables.SinnerID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sinners", schema = "public")
public class SinnerEntity
{
    @Id
    @Column(name = "name", nullable = false)
    private String name;


    @OneToMany(mappedBy = "sinner", cascade = CascadeType.ALL, orphanRemoval = true)
    // TODO, zamiast rekordu musisz użyć encji
    private List<SinnerID> IDs = new ArrayList<>();

    @OneToMany(mappedBy = "sinner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SinnerEGO> EGOs = new ArrayList<>();
}
