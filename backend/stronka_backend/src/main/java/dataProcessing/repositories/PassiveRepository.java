package dataProcessing.repositories;

import dataProcessing.models.PassiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassiveRepository extends JpaRepository<PassiveEntity, UUID>
{

}
