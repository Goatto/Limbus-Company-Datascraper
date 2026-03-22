package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.SinnerEGO;
import dataProcessing.models.join_tables.SinnerEGOID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinnerEGORepository extends JpaRepository<SinnerEGO, SinnerEGOID>
{
}
