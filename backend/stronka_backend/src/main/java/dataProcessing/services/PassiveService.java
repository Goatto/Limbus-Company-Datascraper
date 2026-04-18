package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.PassiveEntity;
import dataProcessing.models.join_tables.PassiveStatusEffects;
import dataProcessing.models.join_tables.PassiveStatusEffectsID;
import dataProcessing.models.StatusEffectEntity;
import dataProcessing.repositories.PassiveRepository;
import dataProcessing.repositories.join_tables.PassiveStatusEffectRepository;
import dataProcessing.repositories.StatusEffectRepository;
import dataProcessing.exceptions.MissingDatabaseEntryException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
// TODO, dodać sprawdzanie czy dane są już w DB
@Slf4j
@Service
public class PassiveService
{
    private final PassiveRepository passiveRepository;
    private final StatusEffectRepository statusEffectRepository;
    private final PassiveStatusEffectRepository passiveStatusEffectRepository;

    public PassiveService(PassiveRepository passiveRepository, StatusEffectRepository statusEffectRepository, PassiveStatusEffectRepository passiveStatusEffectRepository)
    {
        this.passiveRepository = passiveRepository;
        this.statusEffectRepository = statusEffectRepository;
        this.passiveStatusEffectRepository = passiveStatusEffectRepository;
    }

    @Transactional
    public UUID saveNewPassive(ScraperDataDTOs.Passive newPassive)
    {
        PassiveEntity passiveEntity = new PassiveEntity();

        passiveEntity.setName(newPassive.passiveName());
        passiveEntity.setCostType(newPassive.costType());
        passiveEntity.setCost(newPassive.cost());
        passiveEntity.setDescription(newPassive.description());
        PassiveEntity savedEntity = passiveRepository.save(passiveEntity);
        // TODO Dodanie status-effectow na podstawie UUID
        for(String index : newPassive.statusEffects())
        {
            if(statusEffectRepository.findById(index).isEmpty())
            {
                throw new MissingDatabaseEntryException(index);
            }
            StatusEffectEntity savedStatusEffect = statusEffectRepository.getReferenceById(index);
            PassiveStatusEffects passiveStatusEffects = new PassiveStatusEffects();
            passiveStatusEffects.setId(new PassiveStatusEffectsID(savedEntity.getId(), index));
            passiveStatusEffects.setPassive(savedEntity);
            passiveStatusEffects.setStatusEffect(savedStatusEffect);
            PassiveStatusEffects savedPassiveStatusEffect = passiveStatusEffectRepository.save(passiveStatusEffects);
            savedEntity.getStatusEffects().add(savedPassiveStatusEffect);
        }
        savedEntity = passiveRepository.save(savedEntity);
        return savedEntity.getId();

    }
}
