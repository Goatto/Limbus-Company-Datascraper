package dataProcessing.models;

import dataProcessing.ScraperDataDTOs;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

    // Tu będę musiał zrobić OneToMany z racji że jeden sinner ma wiele ID/EGO, a nie w drugą stronę
    /*
    @Column(name = "ids", nullable = true)
    @OneToMany(mappedBy = "id")
    // TODO, zamiast rekordu musisz użyć encji
    private Set<ScraperDataDTOs.IDData> IDs = new HashSet<>();

    @Column(name = "egos", nullable = true)
    @OneToMany
    private Set<ScraperDataDTOs.EGOData> EGOs = new HashSet<>();
     */
}
