package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.MetadataService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ExplorerController {

  private static Logger LOGGER = LoggerFactory.getLogger(ExplorerController.class);

  @Autowired private MetadataService metadataService;
  @Autowired private FetcherService fetcherService;

  @GetMapping(path = "/project", produces = "application/json")
  @ApiOperation("List the project")
  public List<ProjectResponse> getAllProjects() {
    return fetcherService.fetchAllProjects().stream()
        .map(this::toProjectResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}", produces = "application/json")
  @ApiOperation("Get a project for a given projectId")
  public ProjectResponse getProject(@PathVariable final String projectId) {
    return toProjectResponse(fetcherService.fetchProject(projectId));
  }

  @GetMapping(path = "/project/{projectId}/metadata", produces = "application/json")
  @ApiOperation("Get a project for a given projectId")
  public UpdateMetadataProjectRequestResponse getProjectMetadata(
      @PathVariable final String projectId) {
    final boolean activated = metadataService.isProjectActivated(projectId);
    final String planName = activated ? "Enterprise plan" : null; // TODO: Get real plan.
    return new UpdateMetadataProjectRequestResponse(planName, activated);
  }

  @PutMapping(path = "/project/{projectId}/metadata", produces = "application/json")
  @ApiOperation("Update metadata of a project")
  public void updateProject(
      @PathVariable final String projectId,
      @RequestBody final UpdateMetadataProjectRequestResponse request) {
    metadataService.updateProject(projectId, request.isActivated());
  }

  @GetMapping(path = "/project/{projectId}/dataset", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public List<DatasetResponse> getDatasets(@PathVariable final String projectId) {
    return fetcherService.fetchAllDatasets(projectId).stream()
        .map(this::toDatasetResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}/dataset/{datasetName}", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public DatasetResponse getDataset(
      @PathVariable final String projectId, @PathVariable final String datasetName) {
    FetchedDataset fetchedDataset = fetcherService.fetchDataset(projectId, datasetName);
    return toDatasetResponse(fetchedDataset);
  }

  @GetMapping(
      path = "/project/{projectId}/dataset/{datasetName}/table",
      produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public List<TableResponse> getTables(
      @PathVariable final String projectId, @PathVariable final String datasetName) {
    return fetcherService.fetchAllTables(projectId).stream()
        .map(this::toTableResponse)
        .collect(Collectors.toList());
  }

  public ProjectResponse toProjectResponse(FetchedProject project) {
    final String projectId = project.getProjectId();
    return new ProjectResponse(projectId, project.getName());
  }

  public DatasetResponse toDatasetResponse(FetchedDataset dataset) {
    final String projectId = dataset.getProjectId();
    final String datasetName = dataset.getDatasetName();
    final String location = dataset.getLocation();
    final String friendlyName = dataset.getFriendlyName();
    final String description = dataset.getDescription();
    final Long createdAt = dataset.getCreatedAt();
    final Long lastModified = dataset.getLastModified();
    return new DatasetResponse(
        projectId, datasetName, location, friendlyName, description, createdAt, lastModified);
  }

  public TableResponse toTableResponse(FetchedTable table) {
    final String projectId = table.getProjectId();
    final String datasetName = table.getDatasetName();
    final String tableName = table.getTableName();
    return new TableResponse(projectId, datasetName, tableName);
  }
}
