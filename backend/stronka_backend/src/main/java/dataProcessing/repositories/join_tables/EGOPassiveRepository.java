package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.EGOPassive;
import dataProcessing.models.join_tables.EGOPassiveID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EGOPassiveRepository extends JpaRepository<EGOPassive, EGOPassiveID>
{

}
