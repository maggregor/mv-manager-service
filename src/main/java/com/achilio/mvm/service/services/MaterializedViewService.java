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
import javax.transaction.Transactional;
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

  public Optional<MaterializedView> findMaterializedViewByUniqueName(String mvUniqueName) {
    return repository.findByMvUniqueName(mvUniqueName);
  }

  public Optional<MaterializedView> findMaterializedView(Long id, String projectId) {
    return repository.findByIdAndProjectId(id, projectId);
  }

  public List<MaterializedView> getAllMaterializedViews(
      String projectId, String datasetName, String tableName, Long jobId) {
    return repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
        projectId, datasetName, tableName, jobId);
  }

  public MaterializedView getMaterializedView(Long id, String projectId) {
    return findMaterializedView(id, projectId)
        .orElseThrow(() -> new MaterializedViewNotFoundException(id));
  }

  public MaterializedView applyMaterializedView(Long id, String projectId, Connection connection) {
    MaterializedView mv = getMaterializedView(id, projectId);
    try {
      // TODO: Create the view on BigQuery
      createMaterializedView(mv, connection);
      mv.setStatus(MVStatus.APPLIED);
    } catch (Exception e) {
      LOGGER.error("Error during creation of MV {}", mv.getId(), e);
      mv.setStatus(MVStatus.NOT_APPLIED);
      mv.setStatusReason(MVStatusReason.ERROR_DURING_CREATION);
      // TODO: Delete the view from BigQuery for coherence (even if not present)
      deleteMaterializedView(mv, connection);
    }
    return repository.save(mv);
  }

  public MaterializedView unapplyMaterializedView(
      Long id, String projectId, Connection connection) {
    MaterializedView mv = getMaterializedView(id, projectId);
    try {
      deleteMaterializedView(mv, connection);
      mv.setStatus(MVStatus.NOT_APPLIED);
      mv.setStatusReason(MVStatusReason.DELETED_BY_USER);
    } catch (Exception e) {
      LOGGER.error("Error during deletion of MV {}", mv.getId(), e);
      mv.setStatus(MVStatus.UNKNOWN);
      mv.setStatusReason(MVStatusReason.ERROR_DURING_DELETION);
    }
    return saveMaterializedView(mv);
  }

  private void deleteMaterializedView(MaterializedView mv, Connection connection) {
    fetcherService.deleteMaterializedView(mv, connection);
  }

  private void createMaterializedView(MaterializedView mv, Connection connection)
      throws InterruptedException {
    fetcherService.createMaterializedView(mv, connection);
  }

  @Transactional
  public void removeMaterializedView(Long id, String projectId) {
    Optional<MaterializedView> optionalMv = findMaterializedView(id, projectId);
    if (optionalMv.isPresent()) {
      MaterializedView mv = optionalMv.get();
      if (!mv.getStatus().equals(MVStatus.NOT_APPLIED)) {
        throw new MaterializedViewAppliedException(mv.getId());
      }
      deleteMaterializedViewFromDb(mv);
    }
  }

  @Transactional
  public MaterializedView saveMaterializedView(MaterializedView mv) {
    return repository.save(mv);
  }

  @Transactional
  public void deleteMaterializedViewFromDb(MaterializedView mv) {
    repository.delete(mv);
  }

  public MaterializedView addMaterializedView(
      String projectId, String datasetName, String tableName, String statement) {
    ATableId referenceTable = ATableId.of(projectId, datasetName, tableName);
    MaterializedView mv = new MaterializedView(referenceTable, statement);
    return saveMaterializedView(mv);
  }

  public boolean mvExists(MaterializedView m) {
    return findMaterializedViewByUniqueName(m.getMvUniqueName()).isPresent();
  }

  public void saveAllMaterializedViews(List<MaterializedView> toCreateMaterializedViews) {
    repository.saveAll(toCreateMaterializedViews);
  }

  public void flagOutdated(List<MaterializedView> oldMaterializedViews) {
    oldMaterializedViews.stream()
        .filter(MaterializedView::isApplied)
        .forEach(
            m -> {
              m.setStatus(MVStatus.OUTDATED);
              saveMaterializedView(m);
            });
  }

  public void deleteOld(List<MaterializedView> oldMaterializedViews) {
    oldMaterializedViews.stream()
        .filter(MaterializedView::isNotApplied)
        .forEach(this::deleteMaterializedViewFromDb);
  }
}
