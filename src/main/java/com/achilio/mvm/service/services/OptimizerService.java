package com.achilio.mvm.service.services;

import com.achilio.mvm.service.Optimizer;
import com.achilio.mvm.service.OptimizerFactory;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationEvent;
import com.achilio.mvm.service.entities.OptimizationEvent.Type;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractFactory;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@EnableJpaAuditing
@Service
public class OptimizerService {

  private static final int DEFAULT_MAX_MV_GENERATED = 20;
  private static Logger LOGGER = LoggerFactory.getLogger(OptimizerService.class);
  BigQueryMaterializedViewStatementBuilder statementBuilder;
  @Autowired
  private FetcherService fetcherService;
  @Autowired
  private GooglePublisherService publisherService;
  @PersistenceContext(type = PersistenceContextType.EXTENDED)
  private EntityManager entityManager;

  public OptimizerService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public Optimization optimizeProject(final String projectId) throws Exception {
    return optimizeProject(projectId, 30);
  }

  public Optimization optimizeProject(final String projectId, int days) throws Exception {
    LOGGER.info("Run a new optimization");
    FetchedProject project = fetcherService.fetchProject(projectId);
    Optimization o = createNewOptimization(project.getProjectId());
    addOptimizationEvent(o, Type.FETCHING_QUERIES);
    List<FetchedQuery> queries = fetcherService.fetchQueriesSince(projectId, days);
    addOptimizationEvent(o, Type.FETCHING_MODELS);
    Set<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
    addOptimizationEvent(o, Type.FILTER_ELIGIBLE_QUERIES);
    List<FetchedQuery> eligibleQueries = getEligibleQueries(projectId, tables, queries);
    addOptimizationEvent(o, Type.EXTRACTING_FIELDS);
    Set<FieldSet> fieldSets = extractFields(projectId, tables, eligibleQueries);
    addOptimizationEvent(o, Type.OPTIMIZING_FIELDS);
    Set<FieldSet> optimized = optimizeFieldSets(fieldSets);
    addOptimizationEvent(o, Type.BUILDING_OPTIMIZATION);
    List<OptimizationResult> results = buildOptimizationsResults(o, optimized);
    addOptimizationEvent(o, Type.PUBLISHING);
    publish(o, results);
    addOptimizationEvent(o, Type.PUBLISHED);
    LOGGER.info("Optimization {} published with {} MV as proposals.", o.getId(), results.size());
    return o;
  }

  private List<FetchedQuery> getEligibleQueries(String projectId, Set<FetchedTable> tables,
      List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(projectId, tables);
    extractor.analyzeIneligibleReasons(queries);
    return queries.stream().filter(FetchedQuery::isEligible).collect(Collectors.toList());
  }

  private void publish(Optimization o, List<OptimizationResult> results) {
    publisherService.publishOptimization(o, results);
    LOGGER.info("Optimization done with {} results.", results.size());
  }

  private List<OptimizationResult> buildOptimizationsResults(Optimization o, Set<FieldSet> fields) {
    return fields.stream().map(f -> buildOptimizationResult(o, f)).collect(Collectors.toList());
  }

  @Transactional
  public OptimizationResult buildOptimizationResult(Optimization o, FieldSet fieldSet) {
    String statement = statementBuilder.build(fieldSet);
    // To date, get first Table in the set iterator.
    FetchedTable fetchedTable = fieldSet.getReferenceTables().iterator().next();
    OptimizationResult result = new OptimizationResult(o, fetchedTable, statement);
    entityManager.persist(result);
    return result;
  }

  private Set<FieldSet> optimizeFieldSets(Set<FieldSet> fieldSets) {
    Optimizer optimizer = OptimizerFactory.createOptimizer(DEFAULT_MAX_MV_GENERATED);
    return optimizer.optimize(fieldSets);
  }

  private Set<FieldSet> extractFields(
      String project,
      Set<FetchedTable> tables,
      List<FetchedQuery> queries) {
    FieldSetAnalyzer extractor = FieldSetExtractFactory.createFieldSetExtract(project, tables);
    return extractor.extract(queries);
  }

  public Optimization createNewOptimization(final String projectId) {
    Optimization optimization = new Optimization(projectId);
    entityManager.persist(optimization);
    LOGGER.info("New optimization created: {}", optimization.getId());
    return optimization;
  }

  public void addOptimizationEvent(Optimization optimization, Type type) {
    OptimizationEvent event = new OptimizationEvent(optimization, type);
    entityManager.persist(event);
    LOGGER.info("New event on optimization {}: {}", optimization.getId(), type);
  }

}
