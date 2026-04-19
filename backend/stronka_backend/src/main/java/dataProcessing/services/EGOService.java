package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.EGOEntity;
import dataProcessing.models.PassiveEntity;
import dataProcessing.models.join_tables.EGOAbility;
import dataProcessing.models.join_tables.EGOAbilityID;
import dataProcessing.models.join_tables.EGOPassive;
import dataProcessing.models.join_tables.EGOPassiveID;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.EGORepository;
import dataProcessing.repositories.PassiveRepository;
import dataProcessing.repositories.join_tables.EGOAbilityRepository;
import dataProcessing.repositories.join_tables.EGOPassiveRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
// TODO, dodać sprawdzanie czy dane są już w DB
@Service
public class EGOService
{
    private final EGORepository egoRepository;
    private final AbilityRepository abilityRepository;
    private final PassiveRepository passiveRepository;
    private final EGOPassiveRepository egoPassiveRepository;
    private final EGOAbilityRepository egoAbilityRepository;

    public EGOService(EGORepository egoRepository, AbilityRepository abilityRepository, PassiveRepository passiveRepository, EGOPassiveRepository egoPassiveRepository, EGOAbilityRepository egoAbilityRepository)
    {
        this.egoRepository = egoRepository;
        this.abilityRepository = abilityRepository;
        this.passiveRepository = passiveRepository;
        this.egoPassiveRepository = egoPassiveRepository;
        this.egoAbilityRepository = egoAbilityRepository;
    }

    @Transactional
    public void saveNewEGO(ScraperDataDTOs.EGOData newEGO)
    {
        EGOEntity egoEntity = new EGOEntity();

        egoEntity.setName(newEGO.name());
        egoEntity.setSinner(newEGO.sinnerName());
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

        EGOEntity savedEntity = egoRepository.save(egoEntity);
        for(UUID uuid : newEGO.combatPassives())
        {
            PassiveEntity savedPassive = passiveRepository.getReferenceById(uuid);
            EGOPassive egoPassive = new EGOPassive();
            egoPassive.setId(new EGOPassiveID(savedEntity.getName(), uuid));
            egoPassive.setEgo(savedEntity);
            egoPassive.setPassive(savedPassive);
            EGOPassive savedEGOPassive = egoPassiveRepository.save(egoPassive);
            savedEntity.getCombatPassive().add(savedEGOPassive);
        }
        for(UUID uuid : newEGO.abilities())
        {
            AbilityEntity savedAbility = abilityRepository.getReferenceById(uuid);
            EGOAbility egoAbility = new EGOAbility();
            egoAbility.setId(new EGOAbilityID(savedEntity.getName(), uuid));
            egoAbility.setEgo(savedEntity);
            egoAbility.setAbility(savedAbility);
            EGOAbility savedEGOAbility = egoAbilityRepository.save(egoAbility);
            savedEntity.getAbilities().add(savedEGOAbility);
        }

        egoRepository.save(egoEntity);
    }
}
