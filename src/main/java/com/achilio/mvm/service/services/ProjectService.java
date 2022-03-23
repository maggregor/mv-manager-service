package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.stripe.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public List<Project> findAllProjects() {
    return fetcherService.fetchAllProjects().stream()
        .filter(p -> projectExists(p.getProjectId()))
        .map(p -> getProject(p.getProjectId()))
        .collect(Collectors.toList());
  }

  public Project findProjectOrCreate(String projectId) {
    return findProject(projectId).orElseGet(() -> createProject(projectId));
  }

  public Project createProject(String projectId) {
    Project project = new Project(projectId, null, null);
    return projectRepository.save(project);
  }

  public Optional<Project> findProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  public Project getProject(String projectId) {
    fetcherService.fetchProject(projectId);
    return findProject(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  public boolean projectExists(String projectId) {
    return projectRepository.findByProjectId(projectId).isPresent();
  }

  public Project updateProjectSubscription(Project project, String subscriptionId) {
    project.setStripeSubscriptionId(subscriptionId);
    return projectRepository.save(project);
  }

  @Transactional
  public Project updateProject(String projectId, UpdateProjectRequest payload) {
    fetcherService.fetchProject(projectId);
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

  private Optional<ADataset> getDataset(String projectId, String datasetName) {
    return getDataset(getProject(projectId), datasetName);
  }

  private Optional<ADataset> getDataset(Project project, String datasetName) {
    return datasetRepository.findByProjectAndDatasetName(project, datasetName);
  }

  private ADataset findDatasetOrCreate(String projectId, String dataset) {
    return getDataset(projectId, dataset).orElseGet(() -> createDataset(projectId, dataset));
  }

  private ADataset createDataset(String projectId, String datasetName) {
    return createDataset(getProject(projectId), datasetName);
  }

  private ADataset createDataset(Project project, String datasetName) {
    return datasetRepository.save(new ADataset(project, datasetName));
  }

  @Transactional
  public ADataset updateDataset(String projectId, String datasetName, Boolean activated) {
    ADataset dataset = findDatasetOrCreate(projectId, datasetName);
    dataset.setActivated(activated);
    datasetRepository.save(dataset);
    return dataset;
  }

  public boolean isDatasetActivated(String projectId, String datasetName) {
    return getDataset(projectId, datasetName).map(ADataset::isActivated).orElse(false);
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

  public void createProjectFromFetchedProject(FetchedProject p) {
    if (projectExists(p.getProjectId())) {
      Project updatedProject = getProject(p.getProjectId());
      updatedProject.setProjectName(p.getName());
      updatedProject.setOrganization(p.getOrganization());
      projectRepository.save(updatedProject);
    } else {
      projectRepository.save(new Project(p));
    }
  }
}
