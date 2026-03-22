package dataProcessing.services;

import dataProcessing.ScraperDataDTOs;
import dataProcessing.models.AbilityEntity;
import dataProcessing.models.IDEntity;
import dataProcessing.models.PassiveEntity;
import dataProcessing.models.join_tables.IDAbility;
import dataProcessing.models.join_tables.IDAbilityID;
import dataProcessing.models.join_tables.IDPassive;
import dataProcessing.models.join_tables.IDPassiveID;
import dataProcessing.repositories.AbilityRepository;
import dataProcessing.repositories.IDRepository;
import dataProcessing.repositories.PassiveRepository;
import dataProcessing.repositories.join_tables.IDAbilityRepository;
import dataProcessing.repositories.join_tables.IDPassiveRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IDService
{
    private final IDRepository idRepository;
    private final AbilityRepository abilityRepository;
    private final PassiveRepository passiveRepository;
    private final IDPassiveRepository idPassiveRepository;
    private final IDAbilityRepository idAbilityRepository;


    public IDService(IDRepository idRepository, AbilityRepository abilityRepository, PassiveRepository passiveRepository, IDPassiveRepository idPassiveRepository, IDAbilityRepository idAbilityRepository)
    {
        this.idRepository = idRepository;
        this.abilityRepository = abilityRepository;
        this.passiveRepository = passiveRepository;
        this.idPassiveRepository = idPassiveRepository;
        this.idAbilityRepository = idAbilityRepository;
    }

    @Transactional
    public void saveNewID(ScraperDataDTOs.IDData newID)
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
        IDEntity savedEntity = idRepository.save(idEntity);
        for(UUID uuid : newID.combatPassives())
        {
            PassiveEntity savedPassive = passiveRepository.getReferenceById(uuid);
            IDPassive idPassive = new IDPassive();
            idPassive.setId(new IDPassiveID(savedEntity.getName(), uuid));
            idPassive.setIdName(savedEntity);
            idPassive.setPassive(savedPassive);
            IDPassive savedIDPassive = idPassiveRepository.save(idPassive);
            savedEntity.getCombatPassive().add(savedIDPassive);
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
        // abilities


        // idEntity.setSupportPassive();

        idRepository.save(savedEntity);
    }
}
