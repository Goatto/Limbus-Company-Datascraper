package dataProcessing.repositories;

import dataProcessing.models.AbilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AbilityRepository extends JpaRepository<AbilityEntity, UUID>
{

}
