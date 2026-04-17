package dataProcessing.services;

import dataProcessing.models.EGOEntity;
import dataProcessing.models.IDEntity;
import dataProcessing.models.SinnerEntity;
import dataProcessing.models.join_tables.SinnerEGO;
import dataProcessing.models.join_tables.SinnerEGOID;
import dataProcessing.models.join_tables.SinnerID;
import dataProcessing.models.join_tables.SinnerIDID;
import dataProcessing.repositories.EGORepository;
import dataProcessing.repositories.IDRepository;
import dataProcessing.repositories.SinnerRepository;
import dataProcessing.repositories.join_tables.SinnerEGORepository;
import dataProcessing.repositories.join_tables.SinnerIDRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
// TODO, dodać sprawdzanie czy dane są już w DB
@Service
public class SinnerService
{
    private final IDRepository idRepository;
    private final EGORepository egoRepository;
    private final SinnerRepository sinnerRepository;
    private final SinnerIDRepository sinnerIDRepository;
    private final SinnerEGORepository sinnerEGORepository;

    public SinnerService(IDRepository idRepository, EGORepository egoRepository, SinnerRepository sinnerRepository, SinnerIDRepository sinnerIDRepository, SinnerEGORepository sinnerEGORepository) {
        this.idRepository = idRepository;
        this.egoRepository = egoRepository;
        this.sinnerRepository = sinnerRepository;
        this.sinnerIDRepository = sinnerIDRepository;
        this.sinnerEGORepository = sinnerEGORepository;
    }

    @Transactional
    public void buildSinners()
    {
        List<IDEntity> allIDs = idRepository.findAll();
        for(IDEntity idEntity : allIDs)
        {
            String sinnerName = idEntity.getSinner();

            SinnerEntity sinner = sinnerRepository.findById(sinnerName)
                    .orElseGet(() -> {
                        SinnerEntity newSinner = new SinnerEntity();
                        newSinner.setName(sinnerName);
                        return sinnerRepository.save(newSinner);
                    });
            SinnerID sinnerID = new SinnerID();
            sinnerID.setId(new SinnerIDID(sinner.getName(), idEntity.getName()));
            sinnerID.setSinner(sinner);
            sinnerID.setIdName(idEntity);
            SinnerID savedSinnerID = sinnerIDRepository.save(sinnerID);
            sinner.getIDs().add(savedSinnerID);
            sinnerRepository.save(sinner);
        }

        List<EGOEntity> allEGOs = egoRepository.findAll();
        for(EGOEntity egoEntity : allEGOs)
        {
            String sinnerName = egoEntity.getSinner();

            SinnerEntity sinner = sinnerRepository.findById(sinnerName)
                    .orElseGet(() -> {
                        SinnerEntity newSinner = new SinnerEntity();
                        newSinner.setName(sinnerName);
                        return sinnerRepository.save(newSinner);
                    });
            SinnerEGO sinnerEGO = new SinnerEGO();
            sinnerEGO.setId(new SinnerEGOID(sinner.getName(), egoEntity.getName()));
            sinnerEGO.setSinner(sinner);
            sinnerEGO.setEgo(egoEntity);
            SinnerEGO savedSinnerEGO = sinnerEGORepository.save(sinnerEGO);
            sinner.getEGOs().add(savedSinnerEGO);
            sinnerRepository.save(sinner);

        }
    }
}
