package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage project and dataset resources. */
@Service
public class ProjectService {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private DatasetRepository datasetRepository;

  public ProjectService() {}

  public Boolean isProjectActivated(String projectId) {
    Optional<Project> projectMetadata = getProject(projectId);
    return projectMetadata.isPresent() && projectMetadata.get().isActivated();
  }

  public Boolean isProjectAutomatic(String projectId) {
    Optional<Project> projectMetadata = getProject(projectId);
    return projectMetadata.isPresent() && projectMetadata.get().isAutomatic();
  }

  private Optional<Project> getProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  private void registerProjectIfNotExists(String projectId, Boolean activated, Boolean automatic) {
    if (!projectExists(projectId)) {
      Project project = new Project(projectId, activated, automatic, username);
      projectRepository.save(project);
    }
  }

  public boolean projectExists(String projectId) {
    return getProject(projectId).isPresent();
  }

  @Transactional
  public void updateProject(String projectId, Boolean activated, Boolean automatic) {
    registerProjectIfNotExists(projectId, activated, automatic);
    Project project = getProject(projectId).get();
    project.setActivated(activated);
    project.setAutomatic(automatic);
    projectRepository.save(project);
  }

  private Optional<Dataset> getDataset(String projectId, String datasetName) {
    Optional<Project> projectMetadata = getProject(projectId);
    if (!projectMetadata.isPresent()) {
      return Optional.empty();
    }
    return datasetRepository.findByProjectMetadataAndDatasetName(
        projectMetadata.get(), datasetName);
  }

  private boolean datasetExists(Optional<Project> projectMetadata, String datasetName) {
    return projectMetadata
        .filter(
            project ->
                datasetRepository
                    .findByProjectMetadataAndDatasetName(project, datasetName)
                    .isPresent())
        .isPresent();
  }

  private void registerDatasetIfNotExists(String projectId, String datasetName, Boolean activated) {
    Optional<Project> projectMetadata = getProject(projectId);
    if (!datasetExists(projectMetadata, datasetName)) {
      Dataset dataset = new Dataset(projectMetadata.get(), datasetName, activated);
      datasetRepository.save(dataset);
    }
  }

  @Transactional
  public void updateDataset(String projectId, String datasetName, Boolean activated) {
    registerDatasetIfNotExists(projectId, datasetName, activated);
    Dataset dataset = getDataset(projectId, datasetName).get();
    dataset.setActivated(activated);
    datasetRepository.save(dataset);
  }

  public boolean isDatasetActivated(String projectId, String datasetName) {
    Optional<Dataset> datasetMetadata = getDataset(projectId, datasetName);
    return datasetMetadata.map(Dataset::isActivated).orElse(false);
  }
}
