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
import com.google.cloud.bigquery.TableId;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class OptimizerService {

    BigQueryMaterializedViewStatementBuilder statementBuilder;
    @Autowired
    private FetcherService fetcherService;

    public OptimizerService() {
        this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Optimization optimizeProject(final String projectId) {
        Optimization optimization = new Optimization(projectId, true);
        entityManager.persist(optimization);
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.IN_PROGRESS));
        List<FetchedQuery> fetchedQueries = fetcherService.fetchQueries(projectId);
        FieldSetExtract extractor = new ZetaSQLFieldSetExtract(projectId);
        List<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
        extractor.registerTables(tables);
        Set<FieldSet> fieldSets = extractor.extract(fetchedQueries);
        Optimizer optimizer = new BruteForceOptimizer();
        Set<FieldSet> optimized = optimizer.optimize(fieldSets);
        for (FieldSet fieldSet : optimized) {
            if (fieldSet.getTableId() == null) {
                // TODO: Why it's null ?
                continue;
            }
            String dataset = fieldSet.getTableId().getDataset();
            String table = fieldSet.getTableId().getTable();
            String statement = statementBuilder.build(fieldSet);
            OptimizationResult result = new OptimizationResult(dataset, table, optimization, statement);
            entityManager.persist(result);
        }
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.FINISHED));
        return optimization;
    }

}