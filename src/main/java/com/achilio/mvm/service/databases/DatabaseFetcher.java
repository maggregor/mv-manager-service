package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.Table;
import java.util.stream.Stream;

/**
 * Database fetcher interface
 */
public interface DatabaseFetcher {

  /**
   * Returns all the information for a given project.
   */
  FetchedProject fetchProject(String projectId) throws ProjectNotFoundException;

  /**
   * Returns all dataset id in a given projectId.
   */
  Iterable<Dataset> fetchAllDatasets(String projectId);

  Stream<Table> fetchTablesInDataset(String datasetName);

  /**
   * Create Materialized view on BigQuery If view with this name already exists in the dataset, do
   * nothing
   */
  void createMaterializedView(MaterializedView mv);

  /**
   * Delete Materialized view on BigQuery If view with this name already does not exist in the
   * dataset, do nothing
   */
  void deleteMaterializedView(MaterializedView mv);

  /**
   * Dry run a query statement. Throws a BigQueryException if any, indicating that the statement
   * might be invalid
   */
  void dryRunQuery(String query);

  Iterable<Job> fetchJobIterable(long fromTimestamp);
}
