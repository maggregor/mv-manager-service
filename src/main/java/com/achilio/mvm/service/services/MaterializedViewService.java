package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.exceptions.MaterializedViewNotFoundException;
import com.achilio.mvm.service.repositories.MaterializedViewRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MaterializedViewService {
  private final MaterializedViewRepository repository;

  public MaterializedViewService(MaterializedViewRepository repository) {
    this.repository = repository;
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

  public MaterializedView applyMaterializedView(Long id) {
    MaterializedView mv = getMaterializedView(id);
    try {
      // TODO: Create the view on BigQuery
      mv.setStatus(MVStatus.APPLIED);
      mv.setStatusReason(null);
    } catch (Exception e) {
      // TODO: Delete the view from BigQuery for coherence (even if not present)
      mv.setStatus(MVStatus.NOT_APPLIED);
      mv.setStatusReason(MVStatusReason.ERROR);
    }
    return mv;
  }

  public MaterializedView deleteMaterializedView(Long id) {
    return null;
  }
}
