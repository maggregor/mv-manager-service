package com.achilio.mvm.service.services;

import com.achilio.mvm.service.BruteForceOptimizer;
import com.achilio.mvm.service.Optimizer;
import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.controllers.OptimizerController;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationEvent;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.extract.FieldSetExtract;
import com.achilio.mvm.service.extract.ZetaSQLFieldSetExtract;
import com.achilio.mvm.service.extract.fields.FieldSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger LOGGER = LoggerFactory.getLogger(OptimizerService.class);

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
        LOGGER.info("New optimization...");
        FetchedDataset dataset = fetcherService.fetchDataset(projectId, datasetName);
        Optimization optimization = new Optimization(projectId,  dataset.getLocation(), datasetName,true);
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
        //publisherService.publishOptimization(optimization, results, authentication.getCredentials().getAccessToken().toString());
        LOGGER.info("Optimization done with {} results.", results.size());
        return optimization;
    }

}