package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.DatabaseFetcher;
import com.alwaysmart.optimizer.TableMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class OptimizerService implements IOptimizerService {

    @Autowired
    private DatabaseFetcher fetcher;

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
        return null;
    }

    @Override
    public List<TableMetadata> getTableMetadata(String projectId, String datasetName, String tableName) {
        return null;
    }
}
