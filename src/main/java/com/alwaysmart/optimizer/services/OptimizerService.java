package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.BruteForceOptimizer;
import com.alwaysmart.optimizer.DatabaseFetcher;
import com.alwaysmart.optimizer.DatasetMetadata;
import com.alwaysmart.optimizer.FetchedQuery;
import com.alwaysmart.optimizer.FieldSetExtract;
import com.alwaysmart.optimizer.Optimizer;
import com.alwaysmart.optimizer.ProjectMetadata;
import com.alwaysmart.optimizer.TableMetadata;
import com.alwaysmart.optimizer.ZetaSQLFieldSetExtract;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.bigquery.TableId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class OptimizerService implements IOptimizerService {

    @Autowired
    OAuth2AuthorizedClientService clientService;

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
    public Collection<FieldSet> optimizeProject(String project) {
        List<FetchedQuery> fetchedQueries = fetcher().fetchQueries(project);
        FieldSetExtract extractor = new ZetaSQLFieldSetExtract();
        Set<TableId> tableIds = extractor.extractAllTableId(fetchedQueries);
        List<TableMetadata> tables = fetcher().fetchTables(tableIds);
        extractor.registerTables(tables);
        Set<FieldSet> fieldSets = extractor.extract(fetchedQueries);
        Optimizer optimizer = new BruteForceOptimizer();
        Collection<FieldSet> optimized = optimizer.optimize(fieldSets);
        return optimized;
    }

    private DatabaseFetcher fetcher() {
        return new BigQueryDatabaseFetcher(buildGoogleCredentials());
    }

    private GoogleCredentials buildGoogleCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
        AccessToken accessToken = new AccessToken(client.getAccessToken().getTokenValue(), Date.from(client.getAccessToken().getExpiresAt()));
        return UserCredentials.newBuilder()
                .setClientId(client.getClientRegistration().getClientId())
                .setClientSecret(client.getClientRegistration().getClientSecret())
                .setAccessToken(accessToken)
                .build();
    }

}