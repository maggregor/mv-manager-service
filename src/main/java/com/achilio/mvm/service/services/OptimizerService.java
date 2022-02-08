package com.achilio.mvm.service.services;

import com.achilio.mvm.service.Optimizer;
import com.achilio.mvm.service.OptimizerFactory;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationEvent;
import com.achilio.mvm.service.entities.OptimizationEvent.StatusType;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.repositories.OptimizerRepository;
import com.achilio.mvm.service.repositories.OptimizerResultRepository;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.FieldSet;
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

  private static final int DEFAULT_MAX_MV_GENERATED = 5;
  private static Logger LOGGER = LoggerFactory.getLogger(OptimizerService.class);
  BigQueryMaterializedViewStatementBuilder statementBuilder;

  @Autowired private OptimizerResultRepository optimizerResultRepository;
  @Autowired private OptimizerRepository optimizerRepository;

  @Autowired private FetcherService fetcherService;
  @Autowired private GooglePublisherService publisherService;

  @PersistenceContext private EntityManager entityManager;

  public OptimizerService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public Optimization optimizeDataset(String projectId, String datasetName) throws Exception {
    return optimizeDataset(projectId, datasetName, 30);
  }

  public Optimization optimizeDataset(String projectId, String datasetName, int days) {
    LOGGER.info("Run a new optimization on {}", datasetName);
    FetchedProject project = fetcherService.fetchProject(projectId);
    Optimization o = createNewOptimization(project.getProjectId(), datasetName);
    addOptimizationEvent(o, StatusType.FETCHING_QUERIES);
    List<FetchedQuery> queries = fetcherService.fetchQueriesSince(projectId, days);
    addOptimizationEvent(o, StatusType.FETCHING_MODELS);
    Set<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
    List<FetchedQuery> eligibleQueries = getEligibleQueries(projectId, tables, queries);
    addOptimizationEvent(o, StatusType.EXTRACTING_FIELD_SETS);
    List<FieldSet> fieldSets = extractFields(projectId, tables, eligibleQueries);
    addOptimizationEvent(o, StatusType.FILTER_FIELD_SETS_FROM_DATASET);
    fieldSets.removeIf(
        fieldSet ->
            fieldSet.getReferenceTables().stream()
                .anyMatch(table -> !table.getDatasetName().equalsIgnoreCase(datasetName)));
    addOptimizationEvent(o, StatusType.MERGING_FIELD_SETS);
    FieldSetMerger.merge(fieldSets);
    addOptimizationEvent(o, StatusType.OPTIMIZING_FIELD_SETS);
    List<FieldSet> optimized = optimizeFieldSets(fieldSets);
    addOptimizationEvent(o, StatusType.BUILDING_OPTIMIZATION);
    List<OptimizationResult> results = buildOptimizationsResults(o, optimized);
    addOptimizationEvent(o, StatusType.PUBLISHING);
    publish(o, results);
    addOptimizationEvent(o, StatusType.PUBLISHED);
    LOGGER.info("Optimization {} published with {} MV as proposals.", o.getId(), results.size());
    return o;
  }

  private List<FetchedQuery> getEligibleQueries(
      String projectId, Set<FetchedTable> tables, List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    extractor.analyzeIneligibleReasons(queries);
    return queries.stream().filter(FetchedQuery::isEligible).collect(Collectors.toList());
  }

  private void publish(Optimization o, List<OptimizationResult> results) {
    publisherService.publishOptimization(o, results);
    LOGGER.info("Optimization done with {} results.", results.size());
  }

  private List<OptimizationResult> buildOptimizationsResults(
      Optimization o, List<FieldSet> fields) {
    return fields.stream().map(f -> buildOptimizationResult(o, f)).collect(Collectors.toList());
  }

  public OptimizationResult buildOptimizationResult(Optimization o, FieldSet fieldSet) {
    String statement = statementBuilder.build(fieldSet);
    // To date, get first Table in the set iterator.
    FetchedTable fetchedTable = fieldSet.getReferenceTables().iterator().next();
    OptimizationResult result = new OptimizationResult(o, fetchedTable, statement);
    entityManager.persist(result);
    return result;
  }

  private List<FieldSet> optimizeFieldSets(List<FieldSet> fieldSets) {
    Optimizer o = OptimizerFactory.createOptimizerWithDefaultStrategy(DEFAULT_MAX_MV_GENERATED);
    return o.optimize(fieldSets);
  }

  private List<FieldSet> extractFields(
      String project, Set<FetchedTable> tables, List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(project, tables);
    return extractor.extract(queries);
  }

  @Transactional
  public Optimization createNewOptimization(final String projectId, final String datasetName) {
    Optimization optimization = new Optimization(projectId, datasetName);
    entityManager.persist(optimization);
    LOGGER.info(
        "New optimization created: {} on dataset {}",
        optimization.getId(),
        optimization.getDatasetName());
    return optimization;
  }

  public List<Optimization> getAllOptimizationByProject(final String projectId) {
    List<Optimization> optimizations = optimizerRepository.findAllByProjectId(projectId);
    LOGGER.info("Getting all optimizations from project {}", projectId);
    return optimizations;
  }

  public List<Optimization> getAllOptimizationByProjectAndDataset(
      final String projectId, final String datasetName) {
    List<Optimization> optimizations =
        optimizerRepository.findAllByProjectIdAndDatasetName(projectId, datasetName);
    LOGGER.info("Getting all optimizations from project {} and dataset {}", projectId, datasetName);
    return optimizations;
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
