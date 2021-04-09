package com.alwaysmart.optimizer.services;

import java.util.List;

import com.alwaysmart.optimizer.DatasetMetadata;
import com.alwaysmart.optimizer.ProjectMetadata;
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
    List<String> getProjects();

    /**
     * Returns all detail for a given project.
     *
     * @param projectId
     * @return all the information of a project.
     */
    ProjectMetadata getProject(String projectId);


    /**
     * Returns all the datasets for a given project.
     *
     * @param projectId the concerned project id
     *
     * @return all the datasets for a given project.
     */
    List<String> getDatasets(String projectId);

    /**
     * Returns all the information for a given dataset.
     *
     * @param datasetId the concerned dataset id
     *
     * @return all the information for a given dataset.
     */
    DatasetMetadata getDataset(String datasetId);

    /**
     * Returns all the tables for a given project.
     *
     * @param datasetId the concerned dataset id
     *
     * @return all the tables for a given project.
     */
    List<String> getTables(String datasetId);

    /**
     * Returns the metadata for a given table
     *
     * @param tableId the concerned table id
     *
     * @return all the tables for a given project.
     */
    TableMetadata getTableMetadata(String tableId);

}
