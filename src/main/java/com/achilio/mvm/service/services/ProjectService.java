package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage project and dataset resources. */
@Service
public class ProjectService {

  private static Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

  @Autowired private ProjectRepository projectRepository;
  @Autowired private DatasetRepository datasetRepository;
  @Autowired private GooglePublisherService publisherService;
  @Autowired private StripeService stripeService;

  public ProjectService() {}

  public Boolean isProjectActivated(String projectId) {
    return findProjectOrCreate(projectId).isActivated();
  }

  public Boolean isProjectAutomatic(String projectId) {
    return findProjectOrCreate(projectId).isAutomatic();
  }

  public String getProjectUsername(String projectId) {
    return findProjectOrCreate(projectId).getUsername();
  }

  private void publishSchedulers() {
    List<Project> projects = projectRepository.findAllByActivated(true);
    publisherService.publishProjectSchedulers(projects);
  }

  public Project findProjectOrCreate(String projectId) {
    if (!projectExists(projectId)) {
      Project project = new Project(projectId);
      projectRepository.save(project);
    }
    Project project = projectRepository.findByProjectId(projectId).get();
    if (StringUtils.isEmpty(project.getCustomerId())) {
      final String customerId = stripeService.createCustomer(projectId);
      project.setCustomerId(customerId);
      projectRepository.save(project);
    }
    return project;
  }

  public Project getProject(String projectId) {
    return projectRepository.findByProjectId(projectId).get();
  }

  public boolean projectExists(String projectId) {
    return projectRepository.findByProjectId(projectId).isPresent();
  }

  @Transactional
  public void updateProject(
      String projectId,
      Boolean activated,
      Boolean automatic,
      String username,
      Integer analysisTimeframe,
      Integer mvMaxPerTable) {
    Project project = findProjectOrCreate(projectId);
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
    Project project = findProjectOrCreate(projectId);
    return datasetRepository.findByProjectAndDatasetName(project, datasetName);
  }

  private boolean datasetExists(Project project, String datasetName) {
    return datasetRepository.findByProjectAndDatasetName(project, datasetName).isPresent();
  }

  private void registerDatasetIfNotExists(String projectId, String datasetName, Boolean activated) {
    Project project = findProjectOrCreate(projectId);
    if (!datasetExists(project, datasetName)) {
      Dataset dataset = new Dataset(project, datasetName, activated);
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
