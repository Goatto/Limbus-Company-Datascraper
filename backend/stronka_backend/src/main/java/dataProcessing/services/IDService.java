package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.IDEntity;
import dataProcessing.models.PassiveEntity;
import dataProcessing.models.join_tables.*;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.IDRepository;
import dataProcessing.repositories.PassiveRepository;
import dataProcessing.repositories.join_tables.IDAbilityRepository;
import dataProcessing.repositories.join_tables.IDPassiveRepository;
import dataProcessing.repositories.join_tables.IDSupportPassiveRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
// TODO, dodać sprawdzanie czy dane są już w DB
@Service
public class IDService
{
    private final IDRepository idRepository;
    private final AbilityRepository abilityRepository;
    private final PassiveRepository passiveRepository;
    private final IDPassiveRepository idPassiveRepository;
    private final IDSupportPassiveRepository idSupportPassiveRepository;
    private final IDAbilityRepository idAbilityRepository;


    public IDService(IDRepository idRepository, AbilityRepository abilityRepository, PassiveRepository passiveRepository,
                     IDPassiveRepository idPassiveRepository, IDSupportPassiveRepository idSupportPassiveRepository, IDAbilityRepository idAbilityRepository)
    {
        this.idRepository = idRepository;
        this.abilityRepository = abilityRepository;
        this.passiveRepository = passiveRepository;
        this.idPassiveRepository = idPassiveRepository;
        this.idSupportPassiveRepository = idSupportPassiveRepository;
        this.idAbilityRepository = idAbilityRepository;
    }

    @Transactional
    public void saveNewID(ScraperDataDTOs.IDData newID)
    {
        IDEntity idEntity = setBasicInformation(newID);
        IDEntity savedEntity = idRepository.save(idEntity);
        for(UUID uuid : newID.combatPassives())
        {
            getCombatPassive(savedEntity, uuid);
        }

        for(UUID uuid : newID.supportPassives())
        {
            getSupportPassive(savedEntity, uuid);
        }

        for(UUID uuid : newID.abilities())
        {
            AbilityEntity savedAbility = abilityRepository.getReferenceById(uuid);
            IDAbility idAbility = new IDAbility();
            idAbility.setId(new IDAbilityID(savedEntity.getName(), uuid));
            idAbility.setIdName(savedEntity);
            idAbility.setAbility(savedAbility);
            IDAbility savedIDAbility = idAbilityRepository.save(idAbility);
            savedEntity.getAbilities().add(savedIDAbility);
        }

        idRepository.save(savedEntity);
    }

    private static IDEntity setBasicInformation(ScraperDataDTOs.IDData newID)
    {
        IDEntity idEntity = new IDEntity();

        idEntity.setName(newID.name());
        idEntity.setSinner(newID.sinnerName());
        idEntity.setPortraitFile(newID.portraitFile());
        idEntity.setRarity(newID.rarity());
        idEntity.setWorld(newID.world());
        idEntity.setWorldFile(newID.worldFile());
        idEntity.setSeason(newID.season());
        idEntity.setReleaseDate(newID.releaseDate());
        idEntity.setHealth(newID.health());
        idEntity.setSpeed(newID.speed());
        idEntity.setDefenseLevel(newID.defenseLevel());
        idEntity.setTraits(newID.traits());
        idEntity.setStaggerThresholds(newID.staggerThresholds());
        idEntity.setResistances(newID.resistances());
        idEntity.setPositiveSanityEffects(newID.positiveSanityEffects());
        idEntity.setNegativeSanityEffects(newID.negativeSanityEffects());
        return idEntity;
    }

    private void getCombatPassive(IDEntity savedEntity, UUID uuid)
    {
        PassiveEntity savedPassive = passiveRepository.getReferenceById(uuid);
        IDPassive idPassive = new IDPassive();
        idPassive.setId(new IDPassiveID(savedEntity.getName(), uuid));
        idPassive.setIdName(savedEntity);
        idPassive.setPassive(savedPassive);
        IDPassive savedIDPassive = idPassiveRepository.save(idPassive);
        savedEntity.getCombatPassive().add(savedIDPassive);
    }

    private void getSupportPassive(IDEntity savedEntity, UUID uuid)
    {
        PassiveEntity savedPassive = passiveRepository.getReferenceById(uuid);
        IDSupportPassive idSupportPassive = new IDSupportPassive();
        idSupportPassive.setId(new IDSupportPassiveID(savedEntity.getName(), uuid));
        idSupportPassive.setIdName(savedEntity);
        idSupportPassive.setPassive(savedPassive);
        IDSupportPassive savedIDSupportPassive = idSupportPassiveRepository.save(idSupportPassive);
        savedEntity.getSupportPassive().add(savedIDSupportPassive);
    }
}
