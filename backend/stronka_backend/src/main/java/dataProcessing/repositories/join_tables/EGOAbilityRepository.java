package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.EGOAbility;
import dataProcessing.models.join_tables.EGOAbilityID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EGOAbilityRepository extends JpaRepository<EGOAbility, EGOAbilityID>
{
}
