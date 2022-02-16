package com.achilio.mvm.service.services;

import static java.util.stream.Collectors.toList;

import com.achilio.mvm.service.Optimizer;
import com.achilio.mvm.service.OptimizerFactory;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationEvent;
import com.achilio.mvm.service.entities.OptimizationEvent.StatusType;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.entities.OptimizationResult.Status;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.OptimizerRepository;
import com.achilio.mvm.service.repositories.OptimizerResultRepository;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;

/** All the useful services to generate relevant Materialized Views. */
@EnableJpaAuditing
@Service
@Transactional
public class OptimizerService {

  private static final int DEFAULT_PLAN_MAX_MV = 20;
  private static final int GOOGLE_MAX_MV_PER_TABLE = 20;
  private static Logger LOGGER = LoggerFactory.getLogger(OptimizerService.class);
  BigQueryMaterializedViewStatementBuilder statementBuilder;

  @Autowired private OptimizerRepository optimizerRepository;

  @Autowired private OptimizerResultRepository optimizerResultRepository;

  @Autowired private ProjectService projectService;

  @Autowired private FetcherService fetcherService;
  @Autowired private GooglePublisherService publisherService;

  @PersistenceContext private EntityManager entityManager;

  public OptimizerService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public Optimization optimizeProject(String projectId) throws Exception {
    Project project = projectService.getProject(projectId);
    int days = project.getAnalysisTimeframe();
    int maxMvPerTable = Math.min(GOOGLE_MAX_MV_PER_TABLE, project.getMvMaxPerTable());
    List<FetchedDataset> datasets =
        fetcherService.fetchAllDatasets(projectId).parallelStream()
            .filter(
                dataset -> projectService.isDatasetActivated(projectId, dataset.getDatasetName()))
            .collect(toList());
    LOGGER.info("Run a new optimization on {} with activated datasets {}", projectId, datasets);
    FetchedProject fetchedProject = fetcherService.fetchProject(projectId);
    String projectUsername = projectService.getProjectUsername(projectId);
    Optimization o = createNewOptimization(fetchedProject.getProjectId(), projectUsername);
    o.setUsername(projectService.getProjectUsername(projectId));
    LOGGER.info("Username used for optimization {} is {}", o.getId(), projectUsername);
    o.setMvMaxPlan(DEFAULT_PLAN_MAX_MV);
    o.setMvMaxPerTable(maxMvPerTable);
    // STEP 1 - Fetch all queries of targeted fetchedProject
    addOptimizationEvent(o, StatusType.FETCHING_QUERIES);
    List<FetchedQuery> allQueries = fetcherService.fetchQueriesSince(projectId, days);
    // STEP 2 - Fetch all tables
    addOptimizationEvent(o, StatusType.FETCHING_TABLES);
    Set<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
    // STEP 3 - Filter queries from targeted dataset
    addOptimizationEvent(o, StatusType.FILTER_QUERIES_FROM_DATASET);
    FieldSetAnalyzer analyzer = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    allQueries.forEach(analyzer::discoverFetchedTable);
    List<FetchedQuery> allQueriesOnDataset =
        allQueries.stream().filter(query -> isOnDataset(query, datasets)).collect(toList());
    // STEP 4 - Filter eligible queries
    addOptimizationEvent(o, StatusType.FILTER_ELIGIBLE_QUERIES);
    List<FetchedQuery> eligibleQueriesOnDataset =
        getEligibleQueries(projectId, tables, allQueriesOnDataset);
    // STEP 5 - Extract field from queries
    addOptimizationEvent(o, StatusType.EXTRACTING_FIELD_SETS);
    List<FieldSet> fieldSets = extractFields(projectId, tables, eligibleQueriesOnDataset);
    // STEP 6 - Merging same field sets
    addOptimizationEvent(o, StatusType.MERGING_FIELD_SETS);
    fieldSets = FieldSetMerger.merge(fieldSets);
    // STEP 7 - Optimize field sets
    addOptimizationEvent(o, StatusType.OPTIMIZING_FIELD_SETS);
    List<FieldSet> optimized = optimizeFieldSets(fieldSets);
    // STEP 8 - Build materialized views statements
    addOptimizationEvent(o, StatusType.BUILD_MATERIALIZED_VIEWS_STATEMENT);
    List<OptimizationResult> results = buildOptimizationsResults(o, optimized);
    // STEP 9 - Publishing optimization
    applyStatus(o, results);
    Double percent = (double) eligibleQueriesOnDataset.size() / allQueriesOnDataset.size();
    o.setQueryEligiblePercentage(percent);
    addOptimizationEvent(o, StatusType.PUBLISHING);
    List<OptimizationResult> resultsToPublish =
        results.stream().filter(r -> r.getStatus().equals(Status.APPLY)).collect(toList());
    o.setMvAppliedCount(resultsToPublish.size());
    o.setMvProposalCount(
        (int)
            results.stream()
                .filter(r -> !r.getStatus().equals(Status.LIMIT_REACHED_PER_TABLE))
                .count());
    if (publish(o, resultsToPublish)) {
      addOptimizationEvent(o, StatusType.PUBLISHED);
      LOGGER.info(
          "Optimization {} published with {} MV applied. And as {} proposals.",
          o.getId(),
          resultsToPublish.size(),
          results.size());
    } else {
      addOptimizationEvent(o, StatusType.NOT_PUBLISHED);
    }
    return o;
  }

  private void applyStatus(Optimization optimization, List<OptimizationResult> results) {
    // Apply status limit by tables
    results.stream()
        .collect(Collectors.groupingBy(OptimizationResult::getTableId))
        .forEach(
            (key, value) -> {
              value.sort(
                  Comparator.comparingLong(OptimizationResult::getTotalProcessedBytes).reversed());
              value
                  .subList(Math.min(value.size(), optimization.getMvMaxPerTable()), value.size())
                  .forEach(r -> r.setStatus(Status.LIMIT_REACHED_PER_TABLE));
            });
    // Apply status for allowed MV.
    results.stream()
        .filter(OptimizationResult::hasUndefinedStatus)
        .sorted(Comparator.comparingLong(OptimizationResult::getTotalProcessedBytes).reversed())
        .limit(DEFAULT_PLAN_MAX_MV)
        .forEach(r -> r.setStatusIfUndefined(Status.APPLY));
    // Others  MV not allowed: limit plan reached.
    results.stream()
        .filter(OptimizationResult::hasUndefinedStatus)
        .forEach(r -> r.setStatus(Status.PLAN_LIMIT_REACHED));
  }

  private boolean isOnDataset(FetchedQuery query, List<FetchedDataset> datasets) {
    List<String> datasetNames =
        datasets.stream()
            .map(FetchedDataset::getDatasetName)
            .map(String::toLowerCase)
            .collect(toList());
    return query.getReferenceTables().stream()
        .allMatch(d -> datasetNames.contains(d.getDatasetName().toLowerCase()));
  }

  private List<FetchedQuery> getEligibleQueries(
      String projectId, Set<FetchedTable> tables, List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    extractor.analyzeIneligibleReasons(queries);
    return queries.stream().filter(FetchedQuery::isEligible).collect(toList());
  }

  private Boolean publish(Optimization o, List<OptimizationResult> results) {
    LOGGER.info("Optimization done with {} results.", results.size());
    return publisherService.publishOptimization(o, results);
  }

  private List<OptimizationResult> buildOptimizationsResults(
      Optimization o, List<FieldSet> fields) {
    return fields.stream().map(f -> buildOptimizationResult(o, f)).collect(toList());
  }

  public OptimizationResult buildOptimizationResult(Optimization o, FieldSet fieldSet) {
    String statement = statementBuilder.build(fieldSet);
    // To date, get first Table in the set iterator.
    FetchedTable fetchedTable = fieldSet.getReferenceTables().iterator().next();
    OptimizationResult result =
        new OptimizationResult(o, fetchedTable, statement, fieldSet.getStatistics());
    entityManager.persist(result);
    return result;
  }

  private List<FieldSet> optimizeFieldSets(List<FieldSet> fieldSets) {
    Optimizer o = OptimizerFactory.createOptimizerWithDefaultStrategy();
    return o.optimize(fieldSets);
  }

  private List<FieldSet> extractFields(
      String project, Set<FetchedTable> tables, List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(project, tables);
    return extractor.extract(queries);
  }

  @Transactional
  public Optimization createNewOptimization(final String projectId, final String projectUsername) {
    Optimization optimization = new Optimization(projectId, projectUsername);
    entityManager.persist(optimization);
    LOGGER.info("New optimization created: {}", optimization.getId());
    return optimization;
  }

  public List<Optimization> getAllOptimizationByProject(final String projectId) {
    List<Optimization> optimizations = optimizerRepository.findAllByProjectId(projectId);
    LOGGER.info("Getting all optimizations from project {}", projectId);
    return optimizations;
  }

  public void destroyAllMaterializedViewsByProject(final String projectId) {
    LOGGER.info("Ready to destroy");
    publisherService.publishDestroyMaterializedViews(projectId);
  }

  public Optimization getOptimization(final String projectId, final Long optimizationId) {
    Optimization optimization = optimizerRepository.findByProjectIdAndId(projectId, optimizationId);
    LOGGER.info("Getting optimization id: {} from project {}", optimization.getId(), projectId);
    return optimization;
  }

  public List<OptimizationResult> getOptimizationResults(
      final String projectId, final Long optimizationId) {
    List<OptimizationResult> optimizationResults =
        optimizerResultRepository.findAllByProjectIdAndOptimizationId(projectId, optimizationId);
    LOGGER.info("Getting all results of optimization {}", optimizationId);
    return optimizationResults;
  }

  public void addOptimizationEvent(Optimization optimization, StatusType statusType) {
    OptimizationEvent event = new OptimizationEvent(optimization, statusType);
    // TODO: Implement a event system instead of writing sequentially in db
    // entityManager.persist(event);
    LOGGER.info("New event on optimization {}: {}", optimization.getId(), statusType);
  }
}
