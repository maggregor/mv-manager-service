package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.responses.DatasetResponse;
import com.achilio.mvm.service.controllers.responses.GlobalQueryStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.controllers.responses.TableResponse;
import com.achilio.mvm.service.controllers.responses.UpdateMetadataDatasetRequestResponse;
import com.achilio.mvm.service.controllers.responses.UpdateMetadataProjectRequestResponse;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedMaterializedViewEvent;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.FetcherService.StatEntry;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.achilio.mvm.service.services.MetadataService;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ProjectController {

  private static Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

  @Autowired
  private MetadataService metadataService;
  @Autowired
  private FetcherService fetcherService;
  @Autowired
  private GooglePublisherService googlePublisherService;

  @GetMapping(path = "/project", produces = "application/json")
  @ApiOperation("List the project")
  public List<ProjectResponse> getAllProjects() throws Exception {
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

  @PostMapping(path = "/project/{projectId}/metadata", produces = "application/json")
  @ApiOperation("Update metadata of a project")
  public void updateProject(
      @PathVariable final String projectId,
      @RequestBody final UpdateMetadataProjectRequestResponse request)
      throws IOException, ExecutionException, InterruptedException {
    if (request.isActivated()) {
      googlePublisherService.publishProjectActivation(projectId);
    }
    metadataService.updateProject(projectId, request.isActivated());
  }

  @PostMapping(path = "/project/{projectId}/dataset/{datasetName}/metadata", produces = "application/json")
  @ApiOperation("Update metadata of a dataset")
  public void updateDataset(
      @PathVariable final String projectId,
      @PathVariable final String datasetName,
      @RequestBody final UpdateMetadataDatasetRequestResponse request) {
    metadataService.updateDataset(projectId, datasetName, request.isActivated());
  }

  @GetMapping(path = "/project/{projectId}/dataset", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public List<DatasetResponse> getDatasets(@PathVariable final String projectId) throws Exception {
    return fetcherService.fetchAllDatasets(projectId).stream()
        .parallel()
        .map(this::toDatasetResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}/dataset/{datasetName}", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public DatasetResponse getDataset(@PathVariable final String projectId,
      @PathVariable final String datasetName) {
    FetchedDataset fetchedDataset = fetcherService.fetchDataset(projectId, datasetName);
    return toDatasetResponse(fetchedDataset);
  }

  @GetMapping(
      path = "/project/{projectId}/dataset/{datasetName}/table",
      produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public List<TableResponse> getTables(
      @PathVariable final String projectId,
      @PathVariable final String datasetName) {
    return fetcherService.fetchTableNamesInDataset(projectId, datasetName).stream()
        .map(this::toTableResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}/queries/{days}/statistics", produces = "application/json")
  @ApiOperation("Get statistics of queries ")
  public GlobalQueryStatisticsResponse getQueryStatistics(@PathVariable final String projectId,
      @PathVariable final int days) throws Exception {
    GlobalQueryStatistics statistics = fetcherService.getStatistics(projectId, days);
    return toGlobalQueryStatisticsResponse(statistics);
  }

  @GetMapping(path = "/project/{projectId}/queries/{days}/statistics/series", produces = "application/json")
  @ApiOperation("Get statistics of queries grouped per days for charts")
  public List<StatEntry> getDailyStatistics(
      @PathVariable final String projectId,
      @PathVariable final int days) {
    return fetcherService.getDailyStatistics(projectId, days);

  }

  @GetMapping(path = "/project/{projectId}/events/{days}", produces = "application/json")
  @ApiOperation("Get events ")
  public List<FetchedMaterializedViewEvent> getMaterializedViewEvents(
      @PathVariable final String projectId,
      @PathVariable final int days) {
    List<FetchedMaterializedViewEvent> events =
        fetcherService.getMaterializedViewEvents(projectId, days);
    return events;
  }

  @GetMapping(path = "/project/{projectId}/queries/{days}/statistics/eligible", produces = "application/json")
  @ApiOperation("Get statistics of ineligible queries")
  public GlobalQueryStatisticsResponse getEligibleQueryStatistics(
      @PathVariable final String projectId,
      @PathVariable final int days) {
    List<FetchedQuery> queries = fetcherService.fetchQueriesSince(projectId, days);
    Set<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    extractor.analyzeIneligibleReasons(queries);
    GlobalQueryStatistics statistics = fetcherService.getStatistics(queries, true);
    return toGlobalQueryStatisticsResponse(statistics);
  }

  public GlobalQueryStatisticsResponse toGlobalQueryStatisticsResponse(
      GlobalQueryStatistics statistics) {
    return new GlobalQueryStatisticsResponse(statistics);
  }

  public ProjectResponse toProjectResponse(FetchedProject project) {
    final String projectId = project.getProjectId();
    boolean activated = metadataService.isProjectActivated(projectId);
    return new ProjectResponse(projectId, project.getName(), activated);
  }

  public DatasetResponse toDatasetResponse(FetchedDataset dataset) {
    final String projectId = dataset.getProjectId();
    final String datasetName = dataset.getDatasetName();
    final String location = dataset.getLocation();
    final String friendlyName = dataset.getFriendlyName();
    final String description = dataset.getDescription();
    final Long createdAt = dataset.getCreatedAt();
    final Long lastModified = dataset.getLastModified();
    final boolean activated = metadataService.isDatasetActivated(projectId, datasetName);
    return new DatasetResponse(projectId, datasetName, location, friendlyName, description,
        createdAt, lastModified, activated);
  }

  public TableResponse toTableResponse(FetchedTable table) {
    final String projectId = table.getProjectId();
    final String datasetName = table.getDatasetName();
    final String tableName = table.getTableName();
    return new TableResponse(projectId, datasetName, tableName);
  }
}

/**
 * ZoneId defaultZoneId = ZoneId.systemDefault(); LocalDate localDate =
 * LocalDate.now().minusDays(lastDays); Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
 * List<FetchedQuery> queries = fetcherService.fetchQueriesSince(projectId, date);
 * List<FetchedQuery> queriesCaught = queries .stream() .filter(FetchedQuery::isUsingManagedMV)
 * .collect(Collectors.toList()); long totalNumberOfSelect = queries.size(); long numberOfSelectIn =
 * queriesCaught.size(); long numberOfSelectOut = queriesCaught.size(); long totalBilledBytes =
 * queries.stream().mapToLong(fetcherService -> Math.toIntExact(fetcherService.getBilledBytes())).sum();
 * long totalProcessedBytes = queriesCaught.stream().mapToInt(fetcherService ->
 * Math.toIntExact(fetcherService.cost())).sum();
 * <p>
 * return new QueryStatisticsResponse(totalSelect, totalSelectCaught, totalScanned,
 * totalScannedCaught);
 */