package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ADataset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ADatasetRepository extends JpaRepository<ADataset, Integer> {

  Optional<ADataset> findByProjectIdAndDatasetName(String projectId, String datasetName);

  Optional<ADataset> findByDatasetId(String datasetId);

  List<ADataset> findAllByProjectId(String projectId);

  void deleteByDatasetId(String datasetId);
}
