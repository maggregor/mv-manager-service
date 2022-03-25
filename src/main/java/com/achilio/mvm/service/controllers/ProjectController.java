package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.AggregatedStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.DatasetResponse;
import com.achilio.mvm.service.controllers.responses.GlobalQueryStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.controllers.responses.UpdateDatasetRequestResponse;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ProjectController {

  @Autowired private ProjectService projectService;
  @Autowired private FetcherService fetcherService;

  @GetMapping(path = "/project", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("List all projects")
  public List<ProjectResponse> getAllProjects() {
    return projectService.findAllProjects().stream()
        .map(this::toProjectResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a project for a given projectId")
  public ProjectResponse getProject(@PathVariable final String projectId) {
    return toProjectResponse(fetcherService.fetchProject(projectId));
  }

  @GetMapping(
      path = "/project/{projectId}/permissions",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Check permissions for a given projectId")
  public List<String> getMissingPermissions(@PathVariable final String projectId) {
    return fetcherService.fetchMissingPermissions(projectId);
  }

  @PatchMapping(path = "/project/{projectId}")
  @ApiOperation("Update metadata of a project")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ProjectResponse updateProject(
      @PathVariable final String projectId, @RequestBody final UpdateProjectRequest payload) {
    Project updatedProject = projectService.updateProject(projectId, payload);
    return toProjectResponse(updatedProject);
  }

  @PutMapping(
      path = "/project/{projectId}/dataset/{datasetName}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Update metadata of a dataset")
  public UpdateDatasetRequestResponse updateDataset(
      @PathVariable final String projectId,
      @PathVariable final String datasetName,
      @RequestBody final UpdateDatasetRequestResponse payload) {
    return new UpdateDatasetRequestResponse(
        projectService.updateDataset(projectId, datasetName, payload.isActivated()));
  }

  @GetMapping(path = "/project/{projectId}/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get all dataset for a given projectId")
  public List<DatasetResponse> getAllDatasets(@PathVariable final String projectId) {
    return fetcherService.fetchAllDatasets(projectId).stream()
        .map(this::toDatasetResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(
      path = "/project/{projectId}/dataset/{datasetName}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a single dataset for a given projectId")
  public DatasetResponse getDataset(
      @PathVariable final String projectId, @PathVariable final String datasetName) {
    FetchedDataset fetchedDataset = fetcherService.fetchDataset(projectId, datasetName);
    return toDatasetResponse(fetchedDataset);
  }

  @GetMapping(
      path = "/project/{projectId}/queries/{days}/statistics",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get statistics of queries ")
  public GlobalQueryStatisticsResponse getQueryStatistics(
      @PathVariable final String projectId, @PathVariable final int days) throws Exception {
    GlobalQueryStatistics statistics = fetcherService.getStatistics(projectId, days);
    return toGlobalQueryStatisticsResponse(statistics);
  }

  @GetMapping(
      path = "/project/{projectId}/queries/{days}/statistics/kpi",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get statistics of queries grouped per days for charts")
  public AggregatedStatisticsResponse getKPIStatistics(
      @PathVariable final String projectId, @PathVariable final int days) throws Exception {
    GlobalQueryStatistics statistics = fetcherService.getStatistics(projectId, days);
    return toAggregatedStatistics(statistics);
  }

  private GlobalQueryStatisticsResponse toGlobalQueryStatisticsResponse(
      GlobalQueryStatistics statistics) {
    return new GlobalQueryStatisticsResponse(statistics);
  }

  public AggregatedStatisticsResponse toAggregatedStatistics(GlobalQueryStatistics statistics) {
    return new AggregatedStatisticsResponse(statistics);
  }

  private ProjectResponse toProjectResponse(FetchedProject fetchedProject) {
    Project project = projectService.findProjectOrCreate(fetchedProject.getProjectId());
    return new ProjectResponse(fetchedProject.getName(), project);
  }

  private ProjectResponse toProjectResponse(Project project) {
    return new ProjectResponse(project);
  }

  private DatasetResponse toDatasetResponse(FetchedDataset dataset) {
    final String projectId = dataset.getProjectId();
    final String datasetName = dataset.getDatasetName();
    final String location = dataset.getLocation();
    final String friendlyName = dataset.getFriendlyName();
    final String description = dataset.getDescription();
    final Long createdAt = dataset.getCreatedAt();
    final Long lastModified = dataset.getLastModified();
    final boolean activated = projectService.isDatasetActivated(projectId, datasetName);
    return new DatasetResponse(
        projectId,
        datasetName,
        location,
        friendlyName,
        description,
        createdAt,
        lastModified,
        activated);
  }
}

/**
 * ZoneId defaultZoneId = ZoneId.systemDefault(); LocalDate localDate =
 * LocalDate.now().minusDays(lastDays); Date date =
 * Date.from(localDate.atStartOfDay(defaultZoneId).toInstant()); List<FetchedQuery> queries =
 * fetcherService.fetchQueriesSince(projectId, date); List<FetchedQuery> queriesCaught = queries
 * .stream() .filter(FetchedQuery::isUsingManagedMV) .collect(Collectors.toList()); long
 * totalNumberOfSelect = queries.size(); long numberOfSelectIn = queriesCaught.size(); long
 * numberOfSelectOut = queriesCaught.size(); long totalBilledBytes =
 * queries.stream().mapToLong(fetcherService ->
 * Math.toIntExact(fetcherService.getBilledBytes())).sum(); long totalProcessedBytes =
 * queriesCaught.stream().mapToInt(fetcherService -> Math.toIntExact(fetcherService.cost())).sum();
 *
 * <p>return new QueryStatisticsResponse(totalSelect, totalSelectCaught, totalScanned,
 * totalScannedCaught);
 */
