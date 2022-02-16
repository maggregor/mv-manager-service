package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage project and dataset resources. */
@Service
public class ProjectService {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private DatasetRepository datasetRepository;
  @Autowired private GooglePublisherService publisherService;

  public ProjectService() {}

  public Boolean isProjectActivated(String projectId) {
    Optional<Project> project = findProject(projectId);
    return project.isPresent() && project.get().isActivated();
  }

  public Boolean isProjectAutomatic(String projectId) {
    Optional<Project> project = findProject(projectId);
    return project.isPresent() && project.get().isAutomatic();
  }

  public String getProjectUsername(String projectId) {
    Optional<Project> project = findProject(projectId);
    return project.isPresent() ? project.get().getUsername() : "";
  }

  private void publishSchedulers() {
    List<Project> projects = projectRepository.findAllByActivated(true);
    publisherService.publishProjectSchedulers(projects);
  }

  public Optional<Project> findProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  public Project getProject(String projectId) {
    return projectRepository.findByProjectId(projectId).get();
  }

  private void registerProjectIfNotExists(
      String projectId, Boolean activated, Boolean automatic, String username) {
    if (!projectExists(projectId)) {
      Project project = new Project(projectId, activated, automatic, username);
      projectRepository.save(project);
    }
  }

  public boolean projectExists(String projectId) {
    return findProject(projectId).isPresent();
  }

  @Transactional
  public void updateProject(
      String projectId,
      Boolean activated,
      Boolean automatic,
      String username,
      Integer analysisTimeframe,
      Integer mvMaxPerTable) {
    registerProjectIfNotExists(projectId, activated, automatic, username);
    Project project = findProject(projectId).get();
    project.setActivated(activated);
    // If automatic has been sent in the payload (or if the project is being deactivated), we need
    // to publish a potential config change on the schedulers
    Boolean automaticChanged = project.setAutomatic(automatic);
    project.setUsername(username);
    project.setAnalysisTimeframe(analysisTimeframe);
    project.setMvMaxPerTable(mvMaxPerTable);
    projectRepository.save(project);
    if (automaticChanged) {
      publishSchedulers();
    }
  }

  private Optional<Dataset> getDataset(String projectId, String datasetName) {
    Optional<Project> projectMetadata = findProject(projectId);
    if (!projectMetadata.isPresent()) {
      return Optional.empty();
    }
    return datasetRepository.findByProjectAndDatasetName(projectMetadata.get(), datasetName);
  }

  private boolean datasetExists(Optional<Project> project, String datasetName) {
    return project
        .filter(p -> datasetRepository.findByProjectAndDatasetName(p, datasetName).isPresent())
        .isPresent();
  }

  private void registerDatasetIfNotExists(String projectId, String datasetName, Boolean activated) {
    Optional<Project> project = findProject(projectId);
    if (!datasetExists(project, datasetName)) {
      Dataset dataset = new Dataset(project.get(), datasetName, activated);
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
    Optional<Dataset> dataset = getDataset(projectId, datasetName);
    return dataset.map(Dataset::isActivated).orElse(false);
  }
}
