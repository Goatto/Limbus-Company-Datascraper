package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.repositories.AbilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AbilityService
{
    private final AbilityRepository abilityRepository;

    public AbilityService(AbilityRepository abilityRepository)
    {
        this.abilityRepository = abilityRepository;
    }

    // @Transactional - Jeżeli cały zapis nie odbędzie się poprawnie, nic nie zostanie zapisane
    @Transactional
    public AbilityEntity saveNewAbility(ScraperDataDTOs.Ability newAbility)
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

        return abilityRepository.save(abilityEntity);
    }
}
