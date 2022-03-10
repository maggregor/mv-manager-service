package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.stripe.model.Customer;
import com.stripe.model.Product;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage project and dataset resources. */
@Service
public class ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

  @Autowired private ProjectRepository projectRepository;
  @Autowired private DatasetRepository datasetRepository;
  @Autowired private GooglePublisherService publisherService;
  @Autowired private FetcherService fetcherService;
  @Autowired private StripeService stripeService;

  public ProjectService() {}

  private List<Project> getAllActivatedProjects() {
    return projectRepository.findAllByActivated(true);
  }

  public Project findProjectOrCreate(String projectId) {
    return findProject(projectId).orElseGet(() -> createProject(projectId));
  }

  public Project createProject(String projectId) {
    // To date the customerName is the projectId
    Customer customer = stripeService.createCustomer(projectId, projectId);
    Project project = new Project(projectId, customer.getId());
    return projectRepository.save(project);
  }

  public Optional<Project> findProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  public Project getProject(String projectId) {
    return findProject(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  public boolean projectExists(String projectId) {
    return projectRepository.findByProjectId(projectId).isPresent();
  }

  @Deprecated
  @Transactional
  public Project updateProjectOrCreate(String projectId, UpdateProjectRequest payload) {
    FetchedProject fetchedProject = fetcherService.fetchProject(projectId);
    findProjectOrCreate(projectId);
    return updateProject(projectId, payload);
  }

  @Transactional
  public Project updateProject(String projectId, UpdateProjectRequest payload) {
    Project project = getProject(projectId);
    // If automatic has been sent in the payload (or if the project is being deactivated), we need
    // to publish a potential config change on the schedulers
    Boolean automaticChanged = project.setAutomatic(payload.isAutomatic());

    project.setAnalysisTimeframe(payload.getAnalysisTimeframe());
    project.setMvMaxPerTable(payload.getMvMaxPerTable());
    projectRepository.save(project);
    if (automaticChanged) {
      publisherService.publishProjectSchedulers(getAllActivatedProjects());
      if (payload.isAutomatic()) {
        // Automatic mode has just been activated by this current user
        project.setUsername(fetcherService.getUserInfo().getEmail());
      }
    }
    return project;
  }

  private Optional<Dataset> getDataset(String projectId, String datasetName) {
    return getDataset(getProject(projectId), datasetName);
  }

  private Optional<Dataset> getDataset(Project project, String datasetName) {
    return datasetRepository.findByProjectAndDatasetName(project, datasetName);
  }

  private Dataset findDatasetOrCreate(String projectId, String dataset) {
    return getDataset(projectId, dataset).orElseGet(() -> createDataset(projectId, dataset));
  }

  private Dataset createDataset(String projectId, String datasetName) {
    return createDataset(getProject(projectId), datasetName);
  }

  private Dataset createDataset(Project project, String datasetName) {
    return datasetRepository.save(new Dataset(project, datasetName));
  }

  @Transactional
  public Dataset updateDataset(String projectId, String datasetName, Boolean activated) {
    Dataset dataset = findDatasetOrCreate(projectId, datasetName);
    dataset.setActivated(activated);
    datasetRepository.save(dataset);
    return dataset;
  }

  public boolean isDatasetActivated(String projectId, String datasetName) {
    return getDataset(projectId, datasetName).map(Dataset::isActivated).orElse(false);
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
    project.setAutomaticAvailable(false);
    project.setMvMaxPerTableLimit(0);
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
    if (!project.isAutomaticAvailable()) {
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
