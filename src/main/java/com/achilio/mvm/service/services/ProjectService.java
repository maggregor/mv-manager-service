package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.stripe.model.Product;
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
      Boolean automatic,
      String username,
      Integer analysisTimeframe,
      Integer mvMaxPerTable) {
    Project project = findProjectOrCreate(projectId);
    // If automatic has been sent in the payload (or if the project is being deactivated), we need
    // to publish a potential config change on the schedulers
    Boolean automaticChanged = project.setAutomatic(automatic);
    project.setMvMaxPerTable(mvMaxPerTable);
    project.setUsername(username);
    project.setAnalysisTimeframe(analysisTimeframe);
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

  @Transactional
  public void activateProject(Project project) {
    LOGGER.info("Project {} is being activated", project.getProjectId());
    project.setActivated(true);
    projectRepository.save(project);
  }

  @Transactional
  public void deactivateProject(Project project) {
    LOGGER.info(
        "Project {} is being deactivated. Turning off automatic mode", project.getProjectId());
    project.setActivated(false);
    project.setAutomatic(false);
    // TODO: Other cleanup action ?
    projectRepository.save(project);
  }

  @Transactional
  public void updateMvMaxPerTableLimit(Project project, Integer mvMaxPerTableLimit) {
    project.setMvMaxPerTableLimit(mvMaxPerTableLimit);
    if (project.getMvMaxPerTableLimit() < project.getMvMaxPerTable()) {
      project.setMvMaxPerTable(project.getMvMaxPerTableLimit());
    }
    projectRepository.save(project);
  }

  @Transactional
  public void updateProjectAutomaticAvailable(Project project, boolean automaticAvailable) {
    project.setAutomaticAvailable(automaticAvailable);
    if (!project.getAutomaticAvailable()) {
      project.setAutomatic(false);
    }
    projectRepository.save(project);
  }

  // Set and save the plan settings based on the product subscribed to
  public void updatePlanSettings(Project project, Product product) {
    String mvMax = product.getMetadata().get("mv_max");
    String automaticAvailable = product.getMetadata().get("automatic_available");
    if (mvMax != null) {
      updateMvMaxPerTableLimit(project, Integer.valueOf(mvMax));
    }
    if (automaticAvailable != null) {
      updateProjectAutomaticAvailable(project, Boolean.parseBoolean(automaticAvailable));
    }
  }
}
