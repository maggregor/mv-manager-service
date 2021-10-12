package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.DatabaseFetcher;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public interface FetchedQuery {

  String getProjectId();

  void setProjectId(String projectId);

  String getDatasetName();

  void setDatasetName(String datasetName);

  String getTableName();

  void setTableName(String tableName);

  /**
   * The SQL statement of the fetched query.
   *
   * @return the SQL statement of the fetched query.
   */
  String statement();

  /**
   * The query cost weight estimation on the underlying database.
   *
   * @return the query cost on the underlying database.
   */
  long cost();
}
