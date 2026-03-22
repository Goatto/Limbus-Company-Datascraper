package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.PassiveEntity;
import dataProcessing.models.StatusEffectEntity;
import dataProcessing.repositories.PassiveRepository;
import dataProcessing.repositories.StatusEffectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PassiveService
{
    private final PassiveRepository passiveRepository;
    private final StatusEffectRepository statusEffectRepository;

    public PassiveService(PassiveRepository passiveRepository, StatusEffectRepository statusEffectRepository)
    {
        this.passiveRepository = passiveRepository;
        this.statusEffectRepository = statusEffectRepository;
    }

    @Transactional
    public UUID saveNewPassive(ScraperDataDTOs.Passive newPassive)
    {
        PassiveEntity passiveEntity = new PassiveEntity();

        passiveEntity.setName(newPassive.passiveName());
        passiveEntity.setCostType(newPassive.costType());
        passiveEntity.setCost(newPassive.cost());
        passiveEntity.setDescription(newPassive.description());

        // TODO Dodanie status-effectow na podstawie UUID
        for(String index : newPassive.statusEffects())
        {
            StatusEffectEntity savedStatusEffect = statusEffectRepository.getReferenceById(index);
        }

        PassiveEntity savedEntity = passiveRepository.save(passiveEntity);

        return savedEntity.getId();

    }
}
