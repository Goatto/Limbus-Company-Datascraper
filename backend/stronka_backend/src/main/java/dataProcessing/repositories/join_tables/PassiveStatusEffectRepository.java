package dataProcessing.repositories.join_tables;

import dataProcessing.models.join_tables.PassiveStatusEffects;
import dataProcessing.models.join_tables.PassiveStatusEffectsID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassiveStatusEffectRepository extends JpaRepository<PassiveStatusEffects, PassiveStatusEffectsID>
{
}
