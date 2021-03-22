package com.alwaysmart.optimizer.services;

import java.util.List;

import com.alwaysmart.optimizer.TableMetadata;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * All the useful services to generate relevant Materialized Views.
 */
public interface IOptimizerService {

    /**
     * Returns all the projects.
     *
     * @return all the projects.
     */
    List<String> getProjects();


    /**
     * Returns all the datasets for a given project.
     *
     * @param projectId the concerned project id
     *
     * @return all the datasets for a given project.
     */
    List<String> getDatasets(String projectId);

    /**
     * Returns all the tables for a given project.
     *
     * @param projectId the concerned project id
     * @param datasetName the concerned dataset name
     *
     * @return all the tables for a given project.
     */
    List<String> getTables(String projectId, String datasetName);

    /**
     * Returns the metadata for a given table
     *
     * @param projectId the concerned project id
     * @param datasetName the concerned dataset name
     * @param tableName the concerned project id
     *
     * @return all the tables for a given project.
     */
    TableMetadata getTableMetadata(String projectId, String datasetName, String tableName);

}
