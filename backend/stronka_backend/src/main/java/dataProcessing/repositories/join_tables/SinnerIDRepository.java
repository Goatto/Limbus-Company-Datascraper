package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.SinnerID;
import dataProcessing.models.join_tables.SinnerIDID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinnerIDRepository extends JpaRepository<SinnerID, SinnerIDID>
{
}
