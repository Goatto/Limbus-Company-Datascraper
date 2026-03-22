package dataProcessing.repositories;

import dataProcessing.models.IDEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface IDRepository extends JpaRepository<IDEntity, String>
{

}
