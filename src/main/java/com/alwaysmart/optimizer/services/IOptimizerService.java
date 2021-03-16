package com.alwaysmart.optimizer.services;

import java.util.List;

import com.alwaysmart.optimizer.TableMetadata;

/**
 * All the useful services to generate relevant Materialized Views.
 */
public interface IOptimizerService {

    /**
     * Returns all the projects.
     *
     * @return all the projects.
     */
    List<String> getProjects(/* CREDENTIALS */);


    /**
     * Returns all the datasets for a given project.
     *
     * @param projectId the concerned project id
     *
     * @return all the datasets for a given project.
     */
    List<String> getDatasets(/* CREDENTIALS */String projectId);

    /**
     * Returns all the tables for a given project.
     *
     * @param projectId the concerned project id
     * @param datasetName the concerned dataset name
     *
     * @return all the tables for a given project.
     */
    List<String> getTables(/* CREDENTIALS */String projectId, String datasetName);

    /**
     * Returns the metadata for a given table
     *
     * @param projectId the concerned project id
     * @param datasetName the concerned dataset name
     * @param tableName the concerned project id
     *
     * @return all the tables for a given project.
     */
    List<TableMetadata> getTableMetadata(/* CREDENTIALS */String projectId, String datasetName, String tableName);

}
