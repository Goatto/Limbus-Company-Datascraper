package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.IDAbility;
import dataProcessing.models.join_tables.IDAbilityID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDAbilityRepository extends JpaRepository<IDAbility, IDAbilityID>
{
}
