package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.exceptions.MaterializedViewAppliedException;
import com.achilio.mvm.service.exceptions.MaterializedViewNotFoundException;
import com.achilio.mvm.service.repositories.MaterializedViewRepository;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MaterializedViewService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MaterializedViewService.class);

  private final MaterializedViewRepository repository;
  private final FetcherService fetcherService;

  public MaterializedViewService(
      MaterializedViewRepository repository, FetcherService fetcherService) {
    this.repository = repository;
    this.fetcherService = fetcherService;
  }

  public Optional<MaterializedView> findMaterializedView(Long id) {
    return repository.findById(id);
  }

  public List<MaterializedView> getAllMaterializedViews(
      String projectId, String datasetName, String tableName, Long jobId) {
    return repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
        projectId, datasetName, tableName, jobId);
  }

  public MaterializedView getMaterializedView(Long id) {
    return findMaterializedView(id).orElseThrow(() -> new MaterializedViewNotFoundException(id));
  }

  public MaterializedView applyMaterializedView(Long id, Connection connection) {
    MaterializedView mv = getMaterializedView(id);
    try {
      // TODO: Create the view on BigQuery
      createMaterializedView(mv, connection);
      mv.setStatus(MVStatus.APPLIED);
      mv.setStatusReason(null);
    } catch (Exception e) {
      LOGGER.error("Error during creation of MV {}", mv.getId(), e);
      mv.setStatus(MVStatus.NOT_APPLIED);
      mv.setStatusReason(MVStatusReason.ERROR_DURING_CREATION);
      // TODO: Delete the view from BigQuery for coherence (even if not present)
    }
    return repository.save(mv);
  }

  public MaterializedView unapplyMaterializedView(Long id, Connection connection) {
    MaterializedView mv = getMaterializedView(id);
    try {
      deleteMaterializedView(mv, connection);
      mv.setStatus(MVStatus.NOT_APPLIED);
      mv.setStatusReason(MVStatusReason.DELETED_BY_USER);
    } catch (Exception e) {
      LOGGER.error("Error during deletion of MV {}", mv.getId(), e);
      mv.setStatus(MVStatus.UNKNOWN);
      mv.setStatusReason(MVStatusReason.ERROR_DURING_DELETION);
    }
    return repository.save(mv);
  }

  private void deleteMaterializedView(MaterializedView mv, Connection connection) {
    fetcherService.deleteMaterializedView(mv, connection);
  }

  private void createMaterializedView(MaterializedView mv, Connection connection) {
    fetcherService.createMaterializedView(mv, connection);
  }

  public void removeMaterializedView(Long id) {
    Optional<MaterializedView> optionalMv = findMaterializedView(id);
    if (optionalMv.isPresent()) {
      MaterializedView mv = optionalMv.get();
      if (!mv.getStatus().equals(MVStatus.NOT_APPLIED)) {
        throw new MaterializedViewAppliedException(mv.getId());
      }
      repository.delete(mv);
    }
  }

  public MaterializedView addMaterializedView(
      String projectId, String datasetName, String tableName, String statement) {
    ATableId referenceTable = ATableId.of(projectId, datasetName, tableName);
    MaterializedView mv = new MaterializedView(referenceTable, statement);
    return repository.save(mv);
  }
}
