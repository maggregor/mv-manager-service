package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.visitors.ATableId;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class FetcherJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetcherJobService.class);

  @Autowired private FetcherService fetcherService;
  @Autowired private ProjectService projectService;
  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private QueryRepository queryRepository;
  @Autowired private ADatasetRepository datasetRepository;
  @Autowired private ATableRepository tableRepository;

  public FetcherJobService() {}

  public Optional<FetcherQueryJob> getLastFetcherQueryJob(String projectId) {
    return getLastFetcherQueryJob(projectId, null);
  }

  public Optional<FetcherQueryJob> getLastFetcherQueryJob(
      String projectId, FetcherJobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(projectId);
    }
    return fetcherJobRepository.findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public List<FetcherQueryJob> getAllQueryJobs(String projectId, FetcherJobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findFetcherQueryJobsByProjectId(projectId);
    }
    return fetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(projectId, status);
  }

  public Optional<FetcherQueryJob> getFetcherQueryJob(Long fetcherQueryJobId, String projectId) {
    return fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(fetcherQueryJobId, projectId);
  }

  public FetcherQueryJob createNewFetcherQueryJob(
      String projectId, FetcherQueryJobRequest payload) {
    FetcherQueryJob job;
    if (payload.getTimeframe() != null) {
      job = new FetcherQueryJob(projectId, payload.getTimeframe());
    } else {
      job = new FetcherQueryJob(projectId);
    }
    return fetcherJobRepository.save(job);
  }

  @Async("asyncExecutor")
  public void fetchAllQueriesJob(FetcherQueryJob fetcherQueryJob, String teamName) {
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.WORKING);
    try {
      List<Query> queries = fetchQueries(fetcherQueryJob, teamName);
      saveAllQueries(queries);
    } catch (Exception e) {
      updateJobStatus(fetcherQueryJob, FetcherJobStatus.ERROR);
      throw e;
    }
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.FINISHED);
  }

  @Transactional
  void saveAllQueries(List<Query> queries) {
    queryRepository.saveAll(queries);
  }

  private List<Query> fetchQueries(FetcherQueryJob fetcherQueryJob, String teamName) {
    Project project = projectService.getProject(fetcherQueryJob.getProjectId(), teamName);
    List<FetchedQuery> allQueries =
        fetcherService.fetchQueriesSinceLastDays(
            fetcherQueryJob.getProjectId(),
            project.getConnection(),
            fetcherQueryJob.getTimeframe());
    return allQueries.stream()
        .map(q -> toAchilioQuery(q, fetcherQueryJob))
        .collect(Collectors.toList());
  }

  private Query toAchilioQuery(FetchedQuery fetchedQuery, FetcherQueryJob job) {
    return new Query(
        job,
        fetchedQuery.getQuery(),
        fetchedQuery.getGoogleJobId(),
        fetchedQuery.getProjectId(),
        fetchedQuery.getDefaultDataset(),
        fetchedQuery.isUsingMaterializedView(),
        fetchedQuery.isUsingCache(),
        fetchedQuery.getDate(),
        fetchedQuery.getStatistics());
  }

  @Transactional
  void updateJobStatus(FetcherJob job, FetcherJobStatus status) {
    job.setStatus(status);
    fetcherJobRepository.save(job);
  }

  // Struct

  public Optional<FetcherStructJob> getLastFetcherStructJob(
      String projectId, FetcherJobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(projectId);
    }
    return fetcherJobRepository.findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public List<FetcherStructJob> getAllStructJobs(String projectId, FetcherJobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findFetcherStructJobsByProjectId(projectId);
    }
    return fetcherJobRepository.findFetcherStructJobsByProjectIdAndStatus(projectId, status);
  }

  public Optional<FetcherStructJob> getFetcherStructJob(Long fetcherQueryJobId, String projectId) {
    return fetcherJobRepository.findFetcherStructJobByIdAndProjectId(fetcherQueryJobId, projectId);
  }

  public FetcherStructJob createNewFetcherStructJob(String projectId) {
    FetcherStructJob job;
    job = new FetcherStructJob(projectId);
    return fetcherJobRepository.save(job);
  }

  // @Async("asyncExecutor")
  public void syncAllStructsJob(FetcherStructJob fetcherStructJob, String teamName) {
    updateJobStatus(fetcherStructJob, FetcherJobStatus.WORKING);
    try {
      syncDatasets(fetcherStructJob, teamName);
      Set<FetchedTable> allCurrentTables = syncTables(fetcherStructJob);
      syncColumns(fetcherStructJob, teamName, allCurrentTables);
    } catch (Exception e) {
      updateJobStatus(fetcherStructJob, FetcherJobStatus.ERROR);
      throw e;
    }
    updateJobStatus(fetcherStructJob, FetcherJobStatus.FINISHED);
  }

  private void syncColumns(
      FetcherStructJob job, String teamName, Set<FetchedTable> allCurrentTables) {
    Project project = projectService.getProject(job.getProjectId(), teamName);

    // Transform all FetchedColumns to AColumns
    List<AColumn> allCurrentColumns = new ArrayList<>();
    for (FetchedTable table : allCurrentTables) {
      Map<String, String> columns = table.getColumns();
      columns.forEach((k, v) -> allCurrentColumns.add(toAColumn(job, table, k, v)));
    }

    List<AColumn> allAColumns = projectService.getAllColumns(project.getProjectId());
    List<AColumn> toCreateColumn = new ArrayList<>(allCurrentColumns);

    // All columns that don't already exist are created
    toCreateColumn.removeAll(allAColumns);
    projectService.createColumns(toCreateColumn);

    // allAColumns becomes toDeleteColumns after next operation
    allAColumns.removeAll(allCurrentColumns);
    projectService.removeColumns(allAColumns);
  }

  private AColumn toAColumn(FetcherStructJob job, FetchedTable table, String c, String v) {
    ATable localTable = projectService.getTable(table.getTableId().getTableId());
    return new AColumn(job, localTable, c, v);
  }

  @VisibleForTesting
  public Set<FetchedTable> syncTables(FetcherStructJob fetcherStructJob) {
    Project project = projectService.getProject(fetcherStructJob.getProjectId());
    List<ATable> allATables = projectService.getAllTables(project.getProjectId());
    Set<FetchedTable> allFetchedTables =
        fetcherService.fetchAllTables(project.getProjectId(), project.getConnection());
    List<ATable> allCurrentTables =
        allFetchedTables.stream()
            .map(t -> toATable(project, t, fetcherStructJob))
            .collect(Collectors.toList());
    // All fetched tables that already exists are updated with most recent values
    allCurrentTables.stream().filter(this::tableExists).forEach(this::updateTable);

    // All fetched tables that don't already exist are created
    List<ATable> toCreateTables =
        allCurrentTables.stream().filter(t -> !tableExists(t)).collect(Collectors.toList());
    saveAllTables(toCreateTables);

    // allATables becomes toDeleteTables after next operation
    allATables.removeAll(allCurrentTables);
    allATables.forEach(this::deleteTable);

    return allFetchedTables;
  }

  @VisibleForTesting
  public void syncDatasets(FetcherStructJob fetcherStructJob, String teamName) {
    Project project = projectService.getProject(fetcherStructJob.getProjectId(), teamName);
    List<ADataset> allADatasets = projectService.getAllDatasets(project.getProjectId(), teamName);
    List<ADataset> allFetchedDatasets =
        fetcherService
            .fetchAllDatasets(fetcherStructJob.getProjectId(), project.getConnection())
            .stream()
            .map(d -> toADataset(project, d, fetcherStructJob))
            .collect(Collectors.toList());

    // All fetched datasets that already exists are updated with most recent values
    allFetchedDatasets.stream().filter(this::datasetExists).forEach(this::updateDataset);

    // All fetched datasets that don't already exist are created
    List<ADataset> toCreateDatasets =
        allFetchedDatasets.stream().filter(d -> !datasetExists(d)).collect(Collectors.toList());
    saveAllDatasets(toCreateDatasets);

    // allADatasets becomes toDeleteDatasets after next operation
    allADatasets.removeAll(allFetchedDatasets);
    allADatasets.forEach(this::deleteDataset);
  }

  @Transactional
  void updateDataset(ADataset dataset) {
    ADataset existingDataset = projectService.getDataset(dataset.getDatasetId());
    if (existingDataset.getInitialFetcherStructJob() == null) {
      existingDataset.setInitialFetcherStructJob(dataset.getLastFetcherStructJob());
    }
    existingDataset.setDatasetId(dataset.getDatasetId());
    existingDataset.setDatasetName(dataset.getDatasetName());
    existingDataset.setLastFetcherStructJob(dataset.getLastFetcherStructJob());
    datasetRepository.save(existingDataset);
  }

  @Transactional
  void deleteDataset(ADataset toDeleteDataset) {
    projectService.deleteDataset(toDeleteDataset);
  }

  @Transactional
  void saveAllDatasets(List<ADataset> datasets) {
    datasetRepository.saveAll(datasets);
  }

  private boolean datasetExists(ADataset d) {
    Optional<ADataset> dataset =
        datasetRepository.findByProjectAndDatasetName(d.getProject(), d.getDatasetName());
    return dataset.isPresent();
  }

  private ADataset toADataset(
      Project project, FetchedDataset fetchedDataset, FetcherStructJob fetcherStructJob) {
    return new ADataset(fetcherStructJob, project, fetchedDataset.getDatasetName());
  }

  private ATable toATable(
      Project project, FetchedTable fetchedTable, FetcherStructJob fetcherStructJob) {
    ATableId tableId = fetchedTable.getTableId();
    Optional<ADataset> tableDataset =
        projectService.findDataset(project.getProjectId(), tableId.getDataset());
    if (!tableDataset.isPresent()) {
      LOGGER.warn(
          "Dataset {} referenced by Table {} does not exist",
          tableId.getDataset(),
          tableId.getTable());
      return null;
    }
    return new ATable(project, tableDataset.get(), tableId.getTable(), fetcherStructJob);
  }

  private boolean tableExists(ATable t) {
    Optional<ATable> table = projectService.findTable(t.getDataset(), t.getTableName());
    return table.isPresent();
  }

  @Transactional
  void updateTable(ATable table) {
    ATable existingTable = projectService.getTable(table);
    if (existingTable.getInitialFetcherStructJob() == null) {
      existingTable.setInitialFetcherStructJob(table.getLastFetcherStructJob());
    }
    existingTable.setLastFetcherStructJob(table.getLastFetcherStructJob());
    tableRepository.save(existingTable);
  }

  @Transactional
  void saveAllTables(List<ATable> tables) {
    tableRepository.saveAll(tables);
  }

  @Transactional
  void deleteTable(ATable toDeleteTable) {
    projectService.deleteTable(toDeleteTable);
  }
}
