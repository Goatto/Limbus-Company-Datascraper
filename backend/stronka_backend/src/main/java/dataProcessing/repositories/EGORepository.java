package dataProcessing.repositories;

import dataProcessing.models.EGOEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EGORepository extends JpaRepository<EGOEntity, String>
{

}
