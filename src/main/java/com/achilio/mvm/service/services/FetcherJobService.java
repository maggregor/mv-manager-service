package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Job;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class FetcherJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetcherJobService.class);

  private final FetcherService fetcherService;
  private final ProjectService projectService;
  private final FetcherJobRepository fetcherJobRepository;
  private final ADatasetRepository datasetRepository;
  private final ATableRepository tableRepository;

  public FetcherJobService(
      FetcherService fetcherService,
      ProjectService projectService,
      FetcherJobRepository fetcherJobRepository,
      ADatasetRepository datasetRepository,
      ATableRepository tableRepository) {
    this.fetcherService = fetcherService;
    this.projectService = projectService;
    this.fetcherJobRepository = fetcherJobRepository;
    this.datasetRepository = datasetRepository;
    this.tableRepository = tableRepository;
  }

  @Transactional
  void updateJobStatus(Job job, JobStatus status) {
    job.setStatus(status);
    fetcherJobRepository.save(job);
  }

  // Struct

  public Optional<FetcherStructJob> getLastFetcherStructJob(String projectId, JobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(projectId);
    }
    return fetcherJobRepository.findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public List<FetcherStructJob> getAllStructJobs(String projectId, JobStatus status) {
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

  private boolean tableExists(ATable t) {
    Optional<ATable> table = projectService.findTable(t.getDataset(), t.getTableName());
    return table.isPresent();
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
