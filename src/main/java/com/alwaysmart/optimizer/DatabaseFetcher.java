package com.alwaysmart.optimizer;

import java.util.Date;
import java.util.List;

/**
 * Database fetcher interface
 */
public interface DatabaseFetcher {

	/**
	 * Returns history queries for a given dataset.
	 *
	 * @param datasetName - the targeted dataset
	 *
	 * @return - a list of queries as string
	 */
	default List<FetchedQuery> fetchQueries(String projectId, String datasetName) {
		throw new UnsupportedOperationException("Not supported to fetch all tables in dataset.");
	}


	/**
	 * Returns history queries for a given table.
	 *
	 * @param projectId - the targeted project id
	 * @param datasetName - the targeted dataset
	 * @param tableName   - the targeted table
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String projectId, String datasetName, String tableName);

	/**
	 * Returns history queries for a given table and date range.
	 *
	 * @param projectId - the targeted project id
	 * @param datasetName - the targeted dataset
	 * @param tableName   - the targeted table
	 * @param start       - the start range date
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String projectId, String datasetName, String tableName, Date start);

	/**
	 * Returns all metadata of all tables in a given dataset.
	 *
	 * @param projectId - the targeted project id
	 * @param datasetName - the targeted dataset
	 *
	 * @return - all metadata of fetched tables
	 */
	default List<TableMetadata> fetchTablesMetadata(String projectId, String datasetName, String tableName) {
		throw new UnsupportedOperationException("Not supported to fetch all tables in dataset.");
	}

	/**
	 * Returns metadata for a given table.
	 *
	 * @param projectId - the targeted project id
	 * @param datasetName - the targeted dataset
	 * @param tableName   - the targeted tableFe
	 *
	 * @return - TableMetadata of targeted table
	 */
	TableMetadata fetchTableMetadata(String projectId, String datasetName, String tableName);

	/**
	 * Returns all tables in a given dataset.
	 *
	 * @param projectId
	 * @param datasetName
	 * @return
	 */
	List<String> getTables(String projectId, String datasetName);
}
