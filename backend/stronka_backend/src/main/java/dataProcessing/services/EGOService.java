package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.EGOEntity;
import dataProcessing.repositories.EGORepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EGOService
{
    private final EGORepository egoRepository;
    private final AbilityService abilityService;

    public EGOService(EGORepository egoRepository, AbilityService abilityService)
    {
        this.egoRepository = egoRepository;
        this.abilityService = abilityService;
    }

    @Transactional
    public void saveNewEGO(ScraperDataDTOs.EGOData newEGO)
    {
        EGOEntity egoEntity = new EGOEntity();

        egoEntity.setName(newEGO.name());
        egoEntity.setPortraitFile(newEGO.portraitFile());
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

        // TODO zamienić to na dodawanie przez identyfikatory
        for(ScraperDataDTOs.Ability abilityDTO : newEGO.abilities())
        {
            AbilityEntity savedAbility = abilityService.saveNewAbility(abilityDTO);
            egoEntity.addAbility(savedAbility);
        }

        egoRepository.save(egoEntity);
    }
}
