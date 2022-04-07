package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextStripeCustomerId;
import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.AggregatedStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.DatasetResponse;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.controllers.responses.UpdateDatasetRequestResponse;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.services.StripeService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  @Autowired private StripeService stripeService;

  @GetMapping(path = "/project", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all projects")
  public List<ProjectResponse> getAllProjects() {
    return projectService.getAllActivatedProjects(getContextTeamName()).stream()
        .map(this::toProjectResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get a project for a given projectId")
  public ProjectResponse getProject(@PathVariable final String projectId) {
    return toProjectResponse(projectService.getProject(projectId, getContextTeamName()));
  }

  @PostMapping(path = "/project", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Register a project if not exists")
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse createProject(@RequestBody final ACreateProjectRequest payload) {
    Project project = projectService.createProject(payload, getContextTeamName());
    refreshStripeProjectQuantity();
    return toProjectResponse(project);
  }

  private void refreshStripeProjectQuantity() {
    Long quantity = (long) projectService.getAllActivatedProjects(getContextTeamName()).size();
    stripeService.updateSubscriptionQuantity(getContextStripeCustomerId(), quantity);
  }

  @DeleteMapping(path = "/project/{projectId}")
  @ApiOperation("Unregister and delete a project")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProject(@PathVariable final String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    projectService.deleteProject(projectId);
    refreshStripeProjectQuantity();
  }

  @PatchMapping(path = "/project/{projectId}")
  @ApiOperation("Update metadata of a project")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ProjectResponse updateProject(
      @PathVariable final String projectId, @RequestBody final UpdateProjectRequest payload) {
    projectService.getProject(projectId, getContextTeamName());
    Project updatedProject = projectService.updateProject(projectId, payload);
    return toProjectResponse(updatedProject);
  }

  @PutMapping(
      path = "/project/{projectId}/dataset/{datasetName}",
      produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Update metadata of a dataset")
  public UpdateDatasetRequestResponse updateDataset(
      @PathVariable final String projectId,
      @PathVariable final String datasetName,
      @RequestBody final UpdateDatasetRequestResponse payload) {
    projectService.getProject(projectId, getContextTeamName());
    return new UpdateDatasetRequestResponse(
        projectService.updateDataset(projectId, datasetName, payload.isActivated()));
  }

  @GetMapping(path = "/project/{projectId}/dataset", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get all dataset for a given projectId")
  public List<DatasetResponse> getAllDatasets(@PathVariable final String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    return projectService.getAllDatasets(projectId).stream()
        .map(this::toDatasetResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(
      path = "/project/{projectId}/dataset/{datasetName}",
      produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get a single dataset for a given projectId")
  public DatasetResponse getDataset(
      @PathVariable final String projectId, @PathVariable final String datasetName) {
    projectService.getProject(projectId, getContextTeamName());
    FetchedDataset fetchedDataset = projectService.getFetchedDataset(projectId, datasetName);
    return toDatasetResponse(fetchedDataset);
  }

  @GetMapping(
      path = "/project/{projectId}/queries/{days}/statistics/kpi",
      produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get statistics of queries grouped per days for charts")
  public AggregatedStatisticsResponse getKPIStatistics(
      @PathVariable final String projectId, @PathVariable final int days) throws Exception {
    projectService.getProject(projectId, getContextTeamName());
    GlobalQueryStatistics statistics = projectService.getStatistics(projectId, days);
    return toAggregatedStatistics(statistics);
  }

  public AggregatedStatisticsResponse toAggregatedStatistics(GlobalQueryStatistics statistics) {
    return new AggregatedStatisticsResponse(statistics);
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

  private DatasetResponse toDatasetResponse(ADataset dataset) {
    final String projectId = dataset.getProject().getProjectId();
    final String datasetName = dataset.getDatasetName();
    final boolean activated = projectService.isDatasetActivated(projectId, datasetName);
    return new DatasetResponse(projectId, datasetName, null, null, null, null, null, activated);
  }
}
