package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.EGOEntity;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.EGORepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EGOService
{
    private final EGORepository egoRepository;
    private final AbilityRepository abilityRepository;

    public EGOService(EGORepository egoRepository, AbilityService abilityService, AbilityRepository abilityRepository)
    {
        this.egoRepository = egoRepository;
        this.abilityRepository = abilityRepository;
    }

    @Transactional
    public void saveNewEGO(ScraperDataDTOs.EGOData newEGO)
    {
        EGOEntity egoEntity = new EGOEntity();

        egoEntity.setName(newEGO.name());
        egoEntity.setPortraitFile(newEGO.portraitFile());
        egoEntity.setCorrodedPortraitFile(newEGO.corrodedPortraitFile());
        egoEntity.setThreatLevel(newEGO.threatLevel());
        egoEntity.setSeason(newEGO.season());
        egoEntity.setReleaseDate(newEGO.releaseDate());
        egoEntity.setSinAffinity(newEGO.sinAffinity());
        egoEntity.setAbnormality(newEGO.abnormality());
        egoEntity.setAwakenSanityCost(newEGO.awakenSanityCost());
        egoEntity.setCorrosionSanityCost(newEGO.corrosionSanityCost());
        egoEntity.setResistances(newEGO.resistances());
        egoEntity.setAwakenSinCost(newEGO.awakenSinCost());
        egoEntity.setCorrosionSinCost(newEGO.corrosionSinCost());

        for(UUID uuid : newEGO.abilities())
        {
            AbilityEntity savedAbility = abilityRepository.getReferenceById(uuid);
            egoEntity.addAbility(savedAbility);
        }

        egoRepository.save(egoEntity);
    }
}
