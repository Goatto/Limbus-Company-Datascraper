package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.StatusEffectEntity;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.StatusEffectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AbilityService
{
    private final AbilityRepository abilityRepository;
    private final StatusEffectRepository statusEffectRepository;

    public AbilityService(AbilityRepository abilityRepository, StatusEffectRepository statusEffectRepository)
    {
        this.abilityRepository = abilityRepository;
        this.statusEffectRepository = statusEffectRepository;
    }

    // @Transactional - Jeżeli cały zapis nie odbędzie się poprawnie, nic nie zostanie zapisane
    @Transactional
    public UUID saveNewAbility(ScraperDataDTOs.Ability newAbility)
    {
        AbilityEntity abilityEntity = new AbilityEntity();

        abilityEntity.setSkillSlot(newAbility.skillSlot());
        abilityEntity.setAbilityName(newAbility.abilityName());
        abilityEntity.setSinAffinity(newAbility.sinAffinity());
        abilityEntity.setSkillIconFile(newAbility.skillIconFile());
        abilityEntity.setAttackWeight(newAbility.attackWeight());
        abilityEntity.setBasePower(newAbility.basePower());
        abilityEntity.setDamageType(newAbility.damageType());
        abilityEntity.setCoinPower(newAbility.coinPower());
        abilityEntity.setCoinCount(newAbility.coinCount());
        abilityEntity.setOffenseLevel(newAbility.offenseLevel());
        abilityEntity.setBaseEffects(newAbility.baseEffects());
        abilityEntity.setCoinEffects(newAbility.coinEffects());
        for(String index : newAbility.statusEffects())
        {
            StatusEffectEntity savedStatusEffect = statusEffectRepository.getReferenceById(index);
            // abilityEntity.stat
        }


        AbilityEntity savedEntity = abilityRepository.save(abilityEntity);

        return savedEntity.getId();
    }
}
