package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.StatusEffectEntity;
import dataProcessing.repositories.StatusEffectRepository;
import org.springframework.stereotype.Service;

@Service
public class StatusEffectService
{
    private final StatusEffectRepository statusEffectRepository;

    public StatusEffectService(StatusEffectRepository statusEffectRepository)
    {
        this.statusEffectRepository = statusEffectRepository;
    }

    public void saveNewStatusEffect(ScraperDataDTOs.StatusEffect newStatusEffect)
    {
        // Tworzenie pustej encji
        StatusEffectEntity statusEffectEntity = new StatusEffectEntity();

        // Dane z naszego rekordu
        statusEffectEntity.setName(newStatusEffect.name());
        statusEffectEntity.setIcon(newStatusEffect.icon());
        statusEffectEntity.setDescription(newStatusEffect.description());

        // Zapisujemy do bazy danych
        statusEffectRepository.save(statusEffectEntity);
    }
}
