package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ATable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATableRepository extends JpaRepository<ATable, String> {
  List<ATable> findAllByProject_ProjectId(String projectId);

  Optional<ATable> findByProject_ProjectIdAndDataset_DatasetNameAndTableName(
      String projectId, String datasetName, String tableName);

  Optional<ATable> findByTableId(String tableId);

  void deleteByTableId(String tableId);
}
