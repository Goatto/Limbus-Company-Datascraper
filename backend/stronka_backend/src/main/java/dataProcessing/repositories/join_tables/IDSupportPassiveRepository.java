package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.IDSupportPassive;
import dataProcessing.models.join_tables.IDSupportPassiveID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDSupportPassiveRepository extends JpaRepository<IDSupportPassive, IDSupportPassiveID>
{

}
