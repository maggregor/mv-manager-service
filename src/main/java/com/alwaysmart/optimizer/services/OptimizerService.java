package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BruteForceOptimizer;
import com.alwaysmart.optimizer.Optimizer;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.entities.Optimization;
import com.alwaysmart.optimizer.entities.OptimizationEvent;
import com.alwaysmart.optimizer.entities.OptimizationResult;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.ZetaSQLFieldSetExtract;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.TableId;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@EnableJpaAuditing
public class OptimizerService {

    BigQueryMaterializedViewStatementBuilder statementBuilder;
    private FetcherService fetcherService;

    public OptimizerService(GoogleCredentials googleCredentials) {
        this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
        this.fetcherService = new FetcherService(googleCredentials);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String optimizeProject(final String project) {
        Optimization optimization = new Optimization(project, true);
        entityManager.persist(optimization);
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.IN_PROGRESS));
        List<FetchedQuery> fetchedQueries = fetcherService.fetchQueries(project);
        FieldSetExtract extractor = new ZetaSQLFieldSetExtract(project);
        Set<TableId> tableIds = extractor.extractAllTableId(fetchedQueries);
        List<FetchedTable> tables = fetcherService.fetchTables(tableIds);
        extractor.registerTables(tables);
        Set<FieldSet> fieldSets = extractor.extract(fetchedQueries);
        Optimizer optimizer = new BruteForceOptimizer();
        Set<FieldSet> optimized = optimizer.optimize(fieldSets);
        for (FieldSet fieldSet : optimized) {
            String dataset = fieldSet.getTableId().getDataset();
            String table = fieldSet.getTableId().getTable();
            String statement = statementBuilder.build(fieldSet);
            OptimizationResult result = new OptimizationResult(dataset, table, optimization, statement);
            entityManager.persist(result);
        }
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.FINISHED));
        return String.format("{ optimization_id: '%s' }", optimization.getId());
    }

}