package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BruteForceOptimizer;
import com.alwaysmart.optimizer.Optimizer;
import com.alwaysmart.optimizer.configuration.SimpleGoogleCredentialsAuthentication;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.entities.FetchedDataset;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.entities.Optimization;
import com.alwaysmart.optimizer.entities.OptimizationEvent;
import com.alwaysmart.optimizer.entities.OptimizationResult;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.ZetaSQLFieldSetExtract;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.LinkedList;
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
    @Autowired
    private GooglePublisherService publisherService;

    public OptimizerService() {
        this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Optimization optimizeProject(final String projectId) throws Exception {
        throw new Exception("Unsupported method");
    }

    @Transactional
    // TODO: Refacto
    public Optimization optimizeDataset(final String projectId, final String datasetName) throws Exception {
        FetchedDataset dataset = fetcherService.fetchDataset(projectId, datasetName);
        Optimization optimization = new Optimization(projectId,  datasetName, dataset.getLocation(),true);
        entityManager.persist(optimization);
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.IN_PROGRESS));
        List<FetchedQuery> fetchedQueries = fetcherService.fetchQueries(projectId);
        FieldSetExtract extractor = new ZetaSQLFieldSetExtract(projectId);
        List<FetchedTable> tables = fetcherService.fetchAllTables(projectId);
        extractor.registerTables(tables);
        Set<FieldSet> fieldSets = extractor.extract(fetchedQueries);
        Optimizer optimizer = new BruteForceOptimizer();
        Set<FieldSet> optimized = optimizer.optimize(fieldSets);
        List<OptimizationResult> results = new LinkedList<>();
        for (FieldSet fieldSet : optimized) {
            String statement = statementBuilder.build(fieldSet);
            OptimizationResult result = new OptimizationResult(fieldSet.getDataset(), fieldSet.getTable(), optimization, statement);
            results.add(result);
            entityManager.persist(result);
        }
        SimpleGoogleCredentialsAuthentication authentication = (SimpleGoogleCredentialsAuthentication) SecurityContextHolder.getContext().getAuthentication();
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.FINISHED));
        publisherService.publishOptimization(optimization, results, authentication.getCredentials().getAccessToken().toString());
        return optimization;
    }

}