package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.TableMetadata;
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
    public List<String> getTables(String projectId, String datasetName) {
        return new BigQueryDatabaseFetcher().getTables(projectId, datasetName);
    }

    @Override
    public List<TableMetadata> getTableMetadata(String projectId, String datasetName, String tableName) {
        return new BigQueryDatabaseFetcher().fetchTablesMetadata(projectId, datasetName, tableName);
    }
}
