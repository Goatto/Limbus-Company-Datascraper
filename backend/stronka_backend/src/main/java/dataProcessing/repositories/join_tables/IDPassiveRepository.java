package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.IDPassive;
import dataProcessing.models.join_tables.IDPassiveID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDPassiveRepository extends JpaRepository<IDPassive, IDPassiveID>
{

}
