package dataProcessing.repositories;

import dataProcessing.models.SinnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinnerRepository extends JpaRepository<SinnerEntity, String>
{

}
