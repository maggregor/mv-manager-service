package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.DatabaseFetcher;
import com.alwaysmart.optimizer.TableMetadata;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class OptimizerService implements IOptimizerService {

    @Override
    public List<String> getProjects() {
        return null;
    }

    @Override
    public List<String> getDatasets(String projectId) {
        return null;
    }

    @Override
    public List<String> getTables(GoogleCredentials googleCredentials, String projectId, String datasetName) {
        DatabaseFetcher fetcher = new BigQueryDatabaseFetcher(googleCredentials);
        return fetcher.getTables(projectId, datasetName);
    }

    @Override
    public List<TableMetadata> getTableMetadata(GoogleCredentials googleCredentials, String projectId, String datasetName, String tableName) {
        DatabaseFetcher fetcher = new BigQueryDatabaseFetcher(googleCredentials);
        return fetcher.fetchTablesMetadata(projectId, datasetName, tableName);
    }
}
