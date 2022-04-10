package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.MaterializedView;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterializedViewRepository extends JpaRepository<MaterializedView, Long> {

  @Query(
      "SELECT m FROM MaterializedView m WHERE (m.projectId = :projectId) and (:datasetName is null or m.datasetName = :datasetName) and (:tableName is null"
          + " or m.tableName = :tableName) and (:lastJobId is null or m.lastJob.id = :lastJobId)")
  List<MaterializedView> findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
      @Param("projectId") String projectId,
      @Param("datasetName") String datasetName,
      @Param("tableName") String tableName,
      @Param("lastJobId") Long lastJobId);
}
