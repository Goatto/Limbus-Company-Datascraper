package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.join_tables.AbilityStatusEffects;
import dataProcessing.models.join_tables.AbilityStatusEffectsID;
import dataProcessing.models.StatusEffectEntity;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.join_tables.AbilityStatusEffectsRepository;
import dataProcessing.repositories.StatusEffectRepository;
import dataProcessing.exceptions.MissingDatabaseEntryException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

// TODO, dodać sprawdzanie czy dane są już w DB
@Service
public class AbilityService
{
    private final AbilityRepository abilityRepository;
    private final StatusEffectRepository statusEffectRepository;
    private final AbilityStatusEffectsRepository abilityStatusEffectsRepository;

    public AbilityService(AbilityRepository abilityRepository, StatusEffectRepository statusEffectRepository, AbilityStatusEffectsRepository abilityStatusEffectsRepository)
    {
        this.abilityRepository = abilityRepository;
        this.statusEffectRepository = statusEffectRepository;
        this.abilityStatusEffectsRepository = abilityStatusEffectsRepository;
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
        AbilityEntity savedEntity = abilityRepository.save(abilityEntity);
        for(String index : newAbility.statusEffects())
        {
            if(statusEffectRepository.findById(index).isEmpty())
            {
                continue;
            }
            StatusEffectEntity savedStatusEffect = statusEffectRepository.getReferenceById(index);
            AbilityStatusEffects abilityStatusEffects = new AbilityStatusEffects();
            abilityStatusEffects.setId(new AbilityStatusEffectsID(savedEntity.getId(), index));
            abilityStatusEffects.setAbility(savedEntity);
            abilityStatusEffects.setStatusEffect(savedStatusEffect);
            AbilityStatusEffects savedAbilityStatusEffect = abilityStatusEffectsRepository.save(abilityStatusEffects);
            savedEntity.getStatusEffects().add(savedAbilityStatusEffect);
        }
        savedEntity = abilityRepository.save(savedEntity);

        return savedEntity.getId();
    }
}
