package dataProcessing.webScraper.services;

import dataProcessing.webScraper.FormatedScraperData;
import dataProcessing.webScraper.models.StatusEffectEntity;
import dataProcessing.webScraper.repositories.StatusEffectRepository;
import org.springframework.stereotype.Service;

@Service
public class StatusEffectService
{
    private final StatusEffectRepository statusEffectRepository;

    public StatusEffectService(StatusEffectRepository statusEffectRepository)
    {
        this.statusEffectRepository = statusEffectRepository;
    }

    public void saveNewStatusEffect(FormatedScraperData.StatusEffect newStatusEffect)
    {
        // Tworzenie pustej encji
        StatusEffectEntity statusEffectEntity = new StatusEffectEntity();

        // Dane z naszego rekordu
        statusEffectEntity.setName(newStatusEffect.name());
        statusEffectEntity.setIcon(newStatusEffect.icon());
        statusEffectEntity.setDescription(newStatusEffect.description());
        statusEffectEntity.setRelatedEffects(newStatusEffect.relatedEffects());

        // Zapisujemy do bazy danych
        statusEffectRepository.save(statusEffectEntity);
    }
}
