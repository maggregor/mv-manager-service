package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.DatasetMetadata;
import com.achilio.mvm.service.entities.ProjectMetadata;
import com.achilio.mvm.service.repositories.DatasetMetadataRepository;
import com.achilio.mvm.service.repositories.ProjectMetadataRepository;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Services to manage project and dataset resources.
 */
@Service
public class MetadataService {

  @Autowired
  private ProjectMetadataRepository projectRepository;
  @Autowired
  private DatasetMetadataRepository datasetRepository;

  public MetadataService() {
  }

  public Boolean isProjectActivated(String projectId) {
    Optional<ProjectMetadata> projectMetadata = getProject(projectId);
    return projectMetadata.isPresent() && projectMetadata.get().isActivated();
  }

  public Boolean isProjectAutomatic(String projectId) {
    Optional<ProjectMetadata> projectMetadata = getProject(projectId);
    return projectMetadata.isPresent() && projectMetadata.get().isAutomatic();
  }

  private Optional<ProjectMetadata> getProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  private void registerProjectIfNotExists(String projectId, Boolean activated, Boolean automatic) {
    if (!projectExists(projectId)) {
      ProjectMetadata projectMetadata = new ProjectMetadata(projectId, activated, automatic);
      projectRepository.save(projectMetadata);
    }
  }

  public boolean projectExists(String projectId) {
    return getProject(projectId).isPresent();
  }

  @Transactional
  public void updateProject(String projectId, Boolean activated, Boolean automatic) {
    registerProjectIfNotExists(projectId, activated, automatic);
    ProjectMetadata projectMetadata = getProject(projectId).get();
    projectMetadata.setActivated(activated);
    projectMetadata.setAutomatic(automatic);
    projectRepository.save(projectMetadata);
  }

  private Optional<DatasetMetadata> getDataset(String projectId, String datasetName) {
    Optional<ProjectMetadata> projectMetadata = getProject(projectId);
    if (!projectMetadata.isPresent()) {
      return Optional.empty();
    }
    return datasetRepository.findByProjectMetadataAndDatasetName(
        projectMetadata.get(), datasetName);
  }

  private boolean datasetExists(Optional<ProjectMetadata> projectMetadata, String datasetName) {
    return projectMetadata
        .filter(
            project ->
                datasetRepository
                    .findByProjectMetadataAndDatasetName(project, datasetName)
                    .isPresent())
        .isPresent();
  }

  private void registerDatasetIfNotExists(String projectId, String datasetName, Boolean activated) {
    Optional<ProjectMetadata> projectMetadata = getProject(projectId);
    if (!datasetExists(projectMetadata, datasetName)) {
      DatasetMetadata datasetMetadata =
          new DatasetMetadata(projectMetadata.get(), datasetName, activated);
      datasetRepository.save(datasetMetadata);
    }
  }

  @Transactional
  public void updateDataset(String projectId, String datasetName, Boolean activated) {
    registerDatasetIfNotExists(projectId, datasetName, activated);
    DatasetMetadata datasetMetadata = getDataset(projectId, datasetName).get();
    datasetMetadata.setActivated(activated);
    datasetRepository.save(datasetMetadata);
  }

  public boolean isDatasetActivated(String projectId, String datasetName) {
    Optional<DatasetMetadata> datasetMetadata = getDataset(projectId, datasetName);
    return datasetMetadata.map(DatasetMetadata::isActivated).orElse(false);
  }
}
