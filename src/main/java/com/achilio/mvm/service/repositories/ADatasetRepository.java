package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ADatasetRepository extends JpaRepository<ADataset, Integer> {

  Optional<ADataset> findByProjectAndDatasetName(Project project, String datasetName);

  Optional<ADataset> findByProject_ProjectIdAndDatasetName(String projectId, String datasetName);

  Optional<ADataset> findByDatasetId(String datasetId);

  List<ADataset> findAllByProject_ProjectId(String projectId);

  List<ADataset> findAllByProject_ProjectIdAndActivated(String projectId, Boolean activated);

  void deleteByDatasetId(String datasetId);
}
