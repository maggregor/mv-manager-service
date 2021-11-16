package com.achilio.mvm.service.controllers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.MetadataService;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Set;
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

  @Autowired
  private MetadataService metadataService;
  @Autowired
  private FetcherService fetcherService;

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

  @PutMapping(path = "/project/{projectId}/metadata", produces = "application/json")
  @ApiOperation("Update metadata of a project")
  public void updateProject(
      @PathVariable final String projectId,
      @RequestBody final UpdateMetadataProjectRequestResponse request) {
    metadataService.updateProject(projectId, request.isActivated());
  }

  @GetMapping(path = "/project/{projectId}/dataset", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public List<DatasetResponse> getDatasets(@PathVariable final String projectId) throws Exception {
    return fetcherService.fetchAllDatasets(projectId).stream()
        .map(this::toDatasetResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}/dataset/{datasetName}", produces = "application/json")
  @ApiOperation("Get all dataset for a given projectId")
  public DatasetResponse getDataset(
      @PathVariable final String projectId, @PathVariable final String datasetName)
      throws Exception {
    FetchedDataset fetchedDataset = fetcherService.fetchDataset(projectId, datasetName);
    return toDatasetResponse(fetchedDataset);
  }

  @GetMapping(path = "/project/{projectId}/dataset/{datasetName}/mmv/count", produces = "application/json")
  @ApiOperation("Get number of materialized view for given dataset")
  public int getMaterializedViewCount(
      @PathVariable final String projectId, @PathVariable final String datasetName)
      throws Exception {
    return fetcherService.fetchMMVCount(projectId);
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

  @GetMapping(path = "/project/{projectId}/queries/{days}/statistics/eligible", produces = "application/json")
  @ApiOperation("Get statistics of ineligible queries")
  public GlobalQueryStatisticsResponse getEligibleQueryStatistics(
      @PathVariable final String projectId,
      @PathVariable final int days) throws Exception {
    long start, end;
    start = System.currentTimeMillis();
    List<FetchedQuery> queries = fetcherService.fetchQueriesSince(projectId, days);
    end = System.currentTimeMillis();
    System.err.println("Query fetching: " + MILLISECONDS.toSeconds(end - start) + "s.");
    start = System.currentTimeMillis();
    Set<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
    end = System.currentTimeMillis();
    System.err.println("Table fetching: " + MILLISECONDS.toSeconds(end - start) + "s.");
    start = System.currentTimeMillis();
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    end = System.currentTimeMillis();
    System.err.println("Extract: " + MILLISECONDS.toSeconds(end - start) + "s.");
    start = System.currentTimeMillis();
    extractor.analyzeIneligibleReasons(queries);
    end = System.currentTimeMillis();
    System.err.println("Ineligibility: " + MILLISECONDS.toSeconds(end - start) + "s.");
    start = System.currentTimeMillis();
    GlobalQueryStatistics statistics = fetcherService.getStatistics(queries, true);
    end = System.currentTimeMillis();
    System.err.println("Statistics: " + MILLISECONDS.toSeconds(end - start) + "s.");

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