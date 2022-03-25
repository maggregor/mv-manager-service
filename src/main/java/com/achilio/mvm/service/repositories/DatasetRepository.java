package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<ADataset, Integer> {

  Optional<ADataset> findByProjectAndDatasetName(Project project, String datasetName);
}
