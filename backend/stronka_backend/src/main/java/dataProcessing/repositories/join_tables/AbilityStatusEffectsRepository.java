package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.AbilityStatusEffects;
import dataProcessing.models.join_tables.AbilityStatusEffectsID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbilityStatusEffectsRepository extends JpaRepository<AbilityStatusEffects, AbilityStatusEffectsID>
{
}
