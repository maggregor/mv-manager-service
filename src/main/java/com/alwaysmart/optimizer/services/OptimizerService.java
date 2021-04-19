package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.databases.bigquery.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.BruteForceOptimizer;
import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.entities.DatasetMetadata;
import com.alwaysmart.optimizer.databases.FetchedQuery;
import com.alwaysmart.optimizer.entities.Optimization;
import com.alwaysmart.optimizer.entities.OptimizationEvent;
import com.alwaysmart.optimizer.entities.OptimizationResult;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.Optimizer;
import com.alwaysmart.optimizer.databases.entities.ProjectMetadata;
import com.alwaysmart.optimizer.databases.entities.TableMetadata;
import com.alwaysmart.optimizer.extract.ZetaSQLFieldSetExtract;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.bigquery.TableId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
@EnableJpaAuditing // Sure here ?
public class OptimizerService implements IOptimizerService {

    @Autowired
    OAuth2AuthorizedClientService clientService;
    BigQueryMaterializedViewStatementBuilder statementBuilder;

    public OptimizerService() {
        this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> getProjects() {
        return fetcher().fetchProjectIds();
    }

    @Override
    public ProjectMetadata getProject(String projectId) {
        return fetcher().fetchProject(projectId);
    }

    @Override
    public List<String> getDatasets(String projectId) {
        return fetcher().fetchDatasetIds(projectId);
    }

    @Override
    public DatasetMetadata getDataset(String datasetId) {
        return fetcher().fetchDataset(datasetId);
    }

    @Override
    public List<String> getTables(String datasetId) {
        return fetcher().fetchTableIds(datasetId);
    }

    @Override
    public TableMetadata getTableMetadata(String tableId) {
        return fetcher().fetchTable(tableId);
    }

    @Override
    @Transactional
    public String optimizeProject(final String project) {
        Optimization optimization = new Optimization(project, true);
        entityManager.persist(optimization);
        entityManager.persist(new OptimizationEvent(optimization, OptimizationEvent.Type.IN_PROGRESS));
        List<FetchedQuery> fetchedQueries = fetcher(project).fetchQueries(project);
        FieldSetExtract extractor = new ZetaSQLFieldSetExtract(project);
        Set<TableId> tableIds = extractor.extractAllTableId(fetchedQueries);
        List<TableMetadata> tables = fetcher(project).fetchTables(tableIds);
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

    private DatabaseFetcher fetcher() {
        return new BigQueryDatabaseFetcher(buildGoogleCredentials(), null);
    }

    private DatabaseFetcher fetcher(String projectId) {
        return new BigQueryDatabaseFetcher(buildGoogleCredentials(), projectId);
    }

    private GoogleCredentials buildGoogleCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
        AccessToken accessToken = new AccessToken(client.getAccessToken().getTokenValue(), Date.from(Objects.requireNonNull(client.getAccessToken().getExpiresAt())));
        return UserCredentials.newBuilder()
                .setClientId(client.getClientRegistration().getClientId())
                .setClientSecret(client.getClientRegistration().getClientSecret())
                .setAccessToken(accessToken)
                .build();
    }

}