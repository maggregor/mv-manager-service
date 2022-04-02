package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.UserContextHelper.getContextEmail;

import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.exceptions.DatasetNotFoundException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.exceptions.TableNotFoundException;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
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
  @Autowired private ADatasetRepository datasetRepository;
  @Autowired private ATableRepository tableRepository;
  @Autowired private GooglePublisherService publisherService;
  @Autowired private FetcherService fetcherService;
  @Autowired private ConnectionService connectionService;

  public ProjectService() {}

  public List<Project> getAllActivatedProjects(String teamName) {
    return projectRepository.findAllByActivatedAndTeamName(true, teamName);
  }

  public Project getProject(String projectId, String teamName) {
    return findProjectByTeamId(projectId, teamName)
        .orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  @Transactional
  public Project createProject(ACreateProjectRequest payload, String teamName) {
    Connection connection = connectionService.getConnection(payload.getConnectionId(), teamName);
    FetchedProject fetchedProject = fetcherService.fetchProject(payload.getProjectId(), connection);
    return createProjectFromFetchedProject(fetchedProject, teamName, connection);
  }

  /**
   * deleteProject does not actually delete the object in DB but set the project to activated: false
   */
  @Transactional
  public void deleteProject(String projectId, String teamName) {
    projectRepository
        .findByProjectIdAndTeamName(projectId, teamName)
        .ifPresent(this::deactivateProject);
  }

  private Optional<Project> findProjectByTeamId(String projectId, String teamName) {
    return projectRepository.findByProjectIdAndTeamName(projectId, teamName);
  }

  // Old ProjectService

  public List<Project> getAllActivatedProjects() {
    return projectRepository.findAllByActivated(true);
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

  public Project updateProjectSubscription(Project project, String subscriptionId) {
    project.setStripeSubscriptionId(subscriptionId);
    return projectRepository.save(project);
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
        project.setUsername(getContextEmail());
      }
    }
    return project;
  }

  public Optional<ADataset> findDataset(String projectId, String datasetName) {
    return datasetRepository.findByProject_ProjectIdAndDatasetName(projectId, datasetName);
  }

  public ADataset getDataset(String datasetId) {
    return datasetRepository
        .findByDatasetId(datasetId)
        .orElseThrow(() -> new DatasetNotFoundException(datasetId));
  }

  public ADataset getDatasetByProjectAndDatasetName(String projectId, String datasetName) {
    return datasetRepository
        .findByProject_ProjectIdAndDatasetName(projectId, datasetName)
        .orElseThrow(() -> new DatasetNotFoundException(datasetName));
  }

  public FetchedDataset getFetchedDataset(String projectId, String teamName, String datasetName) {
    Project project =
        projectRepository
            .findByProjectIdAndTeamName(projectId, teamName)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
    return fetcherService.fetchDataset(projectId, datasetName, project.getConnection());
  }

  @Transactional
  public ADataset updateDataset(String projectId, String datasetName, Boolean activated) {
    ADataset dataset = getDatasetByProjectAndDatasetName(projectId, datasetName);
    dataset.setActivated(activated);
    datasetRepository.save(dataset);
    return dataset;
  }

  public boolean isDatasetActivated(String projectId, String datasetName) {
    return getDatasetByProjectAndDatasetName(projectId, datasetName).isActivated();
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

  @Transactional
  public Project createProjectFromFetchedProject(
      FetchedProject p, String teamName, Connection connection) {
    Project project;
    if (projectExists(p.getProjectId())) {
      project = getProject(p.getProjectId());
      project.setActivated(true);
    } else {
      project = new Project(p);
      project.setTeamName(teamName);
      project.setConnection(connection);
    }
    return projectRepository.save(project);
  }

  public List<ADataset> getAllDatasets(String projectId, String teamName) {
    getProject(projectId, teamName);
    return datasetRepository.findAllByProject_ProjectId(projectId);
  }

  public GlobalQueryStatistics getStatistics(String projectId, String teamName, int days)
      throws Exception {
    Project project = getProject(projectId, teamName);
    return fetcherService.getStatistics(projectId, project.getConnection(), days);
  }

  public List<ATable> getAllTables(String projectId, String teamName) {
    getProject(projectId, teamName);
    return tableRepository.findAllByProject_ProjectId(projectId);
  }

  public Optional<ATable> findTable(ADataset dataset, String tableName) {
    return tableRepository.findByProject_ProjectIdAndDataset_DatasetNameAndTableName(
        dataset.getProject().getProjectId(), dataset.getDatasetName(), tableName);
  }

  public ATable getTable(ATable table) {
    return tableRepository
        .findByTableId(table.getTableId())
        .orElseThrow(() -> new TableNotFoundException(table.getTableId()));
  }
}
