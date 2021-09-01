package com.alwaysmart.optimizer.databases.entities;

import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.google.cloud.bigquery.TableId;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public interface FetchedQuery {

	String getProjectId();

	String getDatasetName();

	String getTableName();

	void setProjectId(String projectId);

	void setDatasetName(String datasetName);

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
