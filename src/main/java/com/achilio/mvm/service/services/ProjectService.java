package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.DatasetNotFoundException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.exceptions.TableNotFoundException;
import com.achilio.mvm.service.repositories.AColumnRepository;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
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
  @Autowired private AColumnRepository columnRepository;
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
  public void deleteProject(String projectId) {
    projectRepository.findByProjectId(projectId).ifPresent(this::deactivateProject);
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

  @Transactional
  public Project updateProject(String projectId, UpdateProjectRequest payload) {
    Project project = getProject(projectId);
    // If automatic has been sent in the payload (or if the project is being deactivated), we need
    // to publish a potential config change on the schedulers
    project.setAutomatic(payload.isAutomatic());
    project.setAnalysisTimeframe(payload.getAnalysisTimeframe());
    projectRepository.save(project);
    return project;
  }

  public Optional<ADataset> findDataset(String projectId, String datasetName) {
    return datasetRepository.findByProject_ProjectIdAndDatasetName(projectId, datasetName);
  }

  public ADataset getDataset(String projectId, String datasetName) {
    return findDataset(projectId, datasetName)
        .orElseThrow(() -> new DatasetNotFoundException(datasetName));
  }

  public ADataset getDataset(String datasetName) {
    return datasetRepository
        .findByDatasetId(datasetName)
        .orElseThrow(() -> new DatasetNotFoundException(datasetName));
  }

  public ADataset getDatasetByProjectAndDatasetName(String projectId, String datasetName) {
    return datasetRepository
        .findByProject_ProjectIdAndDatasetName(projectId, datasetName)
        .orElseThrow(() -> new DatasetNotFoundException(datasetName));
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
    projectRepository.save(project);
  }

  @Transactional
  public Project createProjectFromFetchedProject(
      FetchedProject p, String teamName, Connection connection) {
    Project project;
    if (projectExists(p.getProjectId())) {
      project = getProject(p.getProjectId());
    } else {
      project = new Project(p);
    }
    project.setActivated(true);
    project.setTeamName(teamName);
    project.setConnection(connection);
    return projectRepository.save(project);
  }

  public List<ADataset> getAllActivatedDatasets(String projectId) {
    return datasetRepository.findAllByProject_ProjectIdAndActivated(projectId, true);
  }

  public List<ADataset> getAllDatasets(String projectId) {
    return datasetRepository.findAllByProject_ProjectId(projectId);
  }

  @Transactional
  public void deleteDataset(ADataset d) {
    datasetRepository.deleteByDatasetId(d.getDatasetId());
  }

  @Transactional
  public void deleteTable(ATable toDeleteTable) {
    tableRepository.deleteByTableId(toDeleteTable.getTableId());
  }

  public List<ATable> getAllTables(String projectId) {
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

  public ATable getTable(String tableId) {
    return tableRepository
        .findByTableId(tableId)
        .orElseThrow(() -> new TableNotFoundException(tableId));
  }

  public List<AColumn> getAllColumns(String projectId) {
    return columnRepository.findAllByTable_Project_ProjectId(projectId);
  }

  @Transactional
  public void createColumns(List<AColumn> toCreateColumn) {
    columnRepository.saveAll(toCreateColumn);
  }

  @Transactional
  public void removeColumns(List<AColumn> allAColumns) {
    columnRepository.deleteAll(allAColumns);
  }
}
