package com.achilio.mvm.service.services;

import static java.util.stream.Collectors.toList;

import com.achilio.mvm.service.MVFactory;
import com.achilio.mvm.service.MVGenerator;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.OptimizationEvent.StatusType;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.FindMVJobNotFoundException;
import com.achilio.mvm.service.repositories.FindMVJobRepository;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.FieldSetExtract;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FindMVJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindMVJobService.class);

  private final FindMVJobRepository repository;
  private final ProjectService projectService;
  private final QueryService queryService;
  private final MaterializedViewService materializedViewService;
  BigQueryMaterializedViewStatementBuilder statementBuilder;

  public FindMVJobService(
      FindMVJobRepository repository,
      ProjectService projectService,
      QueryService queryService,
      MaterializedViewService materializedViewService) {
    this.repository = repository;
    this.projectService = projectService;
    this.queryService = queryService;
    this.materializedViewService = materializedViewService;
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  private Optional<FindMVJob> findMVJob(Long id, String projectId) {
    return repository.findByIdAndProjectId(id, projectId);
  }

  private Optional<FindMVJob> findLastMVJob(String projectId) {
    return repository.findTopByProjectIdOrderByCreatedAtDesc(projectId);
  }

  private Optional<FindMVJob> findLastMVJob(String projectId, JobStatus status) {
    if (status == null) {
      return findLastMVJob(projectId);
    }
    return repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
  }

  public List<FindMVJob> getAllMVJobs(String projectId, JobStatus status) {
    if (status != null) {
      return repository.findAllByProjectIdAndStatus(projectId, status);
    }
    return repository.findAllByProjectId(projectId);
  }

  public FindMVJob getLastMVJobByStatus(String projectId, JobStatus status) {
    return findLastMVJob(projectId, status)
        .orElseThrow(() -> new FindMVJobNotFoundException("last"));
  }

  public FindMVJob getMVJob(Long id, String projectId) {
    return findMVJob(id, projectId)
        .orElseThrow(() -> new FindMVJobNotFoundException(id.toString()));
  }

  @Transactional
  public FindMVJob createMVJob(String projectId, int timeframe) {
    FindMVJob job = new FindMVJob(projectId, timeframe);
    return saveFindMVJob(job);
  }

  @Async("asyncExecutor")
  public void startFindMVJob(FindMVJob job) {
    try {
      String projectId = job.getProjectId();
      int analysisTimeframe = job.getTimeframe();
      List<ADataset> datasets = projectService.getAllActivatedDatasets(projectId);
      LOGGER.info("Start FindMV Job on project {} with activated datasets {}", projectId, datasets);
      LOGGER.info("FindMV Job {} on last {} days", job.getId(), job.getTimeframe());
      // STEP 1 - Fetch all queries of targeted fetchedProject
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.FETCHING_QUERIES);
      LocalDate date = LocalDate.now().minusDays(analysisTimeframe);
      List<Query> allQueries = queryService.getAllQueriesSince(projectId, date);
      // STEP 2 - Fetch all tables
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.FETCHING_TABLES);
      Set<ATable> tables = new HashSet<>(projectService.getAllTables(projectId));
      // STEP 3 - Extract field from queries
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.EXTRACTING_FIELD_SETS);
      List<FieldSet> allFieldSets = extractFields(tables, allQueries);
      // STEP 4 - Filter eligible fieldSets
      List<FieldSet> fieldSets =
          allFieldSets.stream().filter(FieldSet::isEligible).collect(toList());
      // STEP 5 - Merging same field sets
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.MERGING_FIELD_SETS);
      List<FieldSet> distinctFieldSets = FieldSetMerger.mergeSame(fieldSets);
      // STEP 6 - Optimize field sets
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.GENERATING_MVs);
      List<FieldSet> generateMVs = generateMVs(distinctFieldSets);
      // STEP 7 - Build materialized views statements
      LOGGER.info("Find MV Job {}: {}", job.getId(), StatusType.BUILD_MATERIALIZED_VIEWS_STATEMENT);
      List<MaterializedView> results = buildAllMaterializedViews(job, generateMVs);
      // STEP 8 - Publishing optimization
      job.setMvProposalCount(results.size());
      // STEP 9 - Save all Materialized Views
      mergeAndSaveAllMaterializedViews(projectId, results);
      LOGGER.info("Find MV Job {} finished with {} results", job.getId(), job.getMvProposalCount());
      job.setStatus(JobStatus.FINISHED);
    } catch (Exception e) {
      job.setStatus(JobStatus.ERROR);
      LOGGER.error("FindMV Job {} failed", job.getId(), e);
    }
    saveFindMVJob(job);
  }

  private void mergeAndSaveAllMaterializedViews(String projectId, List<MaterializedView> results) {
    // STEP 1: Find existingMaterializedViews in results
    List<MaterializedView> existingMaterializedViews =
        materializedViewService.getAllMaterializedViews(projectId, null, null, null);
    // STEP 2: Find and create newMaterializedViews
    List<MaterializedView> resultsCopy = new ArrayList<>(results);
    createNewMaterializedViews(existingMaterializedViews, resultsCopy);

    // STEP 3: Out of the existingMV, find the ones not in results
    List<MaterializedView> oldMaterializedViews =
        getOldMaterializedViews(existingMaterializedViews, results);

    // STEP 4: Out of the existingMV, find and flag the APPLIED to OUTDATED
    flagOutdatedMaterializedViews(oldMaterializedViews);

    // STEP 5: Out of the oldMaterializedViews, find and delete the NOT_APPLIED
    deleteOldMaterializedViews(oldMaterializedViews);
  }

  private void deleteOldMaterializedViews(List<MaterializedView> oldMaterializedViews) {
    materializedViewService.deleteOld(oldMaterializedViews);
  }

  private void flagOutdatedMaterializedViews(List<MaterializedView> oldMaterializedViews) {
    materializedViewService.flagOutdated(oldMaterializedViews);
  }

  private List<MaterializedView> getOldMaterializedViews(
      List<MaterializedView> existingMaterializedViews, List<MaterializedView> results) {
    existingMaterializedViews.removeAll(results);
    return existingMaterializedViews;
  }

  private void createNewMaterializedViews(
      List<MaterializedView> existingMaterializedViews, List<MaterializedView> results) {
    // Remove all existing MV from the results to get all the new ones
    results.removeAll(existingMaterializedViews);
    materializedViewService.saveAllMaterializedViews(results);
  }

  @Transactional
  void filterAndSaveAllMaterializedViews(List<MaterializedView> allMVs) {
    allMVs.stream()
        .filter(mv -> !materializedViewService.mvExists(mv))
        .forEach(materializedViewService::saveMaterializedView);
  }

  @Transactional
  FindMVJob saveFindMVJob(FindMVJob job) {
    return repository.save(job);
  }

  private List<MaterializedView> buildAllMaterializedViews(FindMVJob job, List<FieldSet> fields) {
    return fields.stream().map(f -> buildMaterializedView(job, f)).collect(toList());
  }

  private MaterializedView buildMaterializedView(FindMVJob job, FieldSet fieldSet) {
    String statement = statementBuilder.build(fieldSet);
    // To date, get first Table in the set iterator.
    ATableId tableId = fieldSet.getReferenceTable();
    return new MaterializedView(job, tableId, statement, fieldSet.getHits());
  }

  private List<FieldSet> generateMVs(List<FieldSet> fieldSets) {
    MVGenerator generator = MVFactory.createMVsWithDefaultStrategy();
    return generator.generate(fieldSets);
  }

  private List<FieldSet> extractFields(Set<ATable> tables, List<Query> queries) {
    FieldSetExtract extractor = FieldSetExtractFactory.createFieldSetExtract(tables);
    return extractor.extractAll(queries);
  }
}
