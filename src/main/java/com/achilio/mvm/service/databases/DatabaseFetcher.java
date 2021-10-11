package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;

import java.util.Date;
import java.util.List;

/**
 * Database fetcher interface
 */
public interface DatabaseFetcher {

	/**
	 * Returns history queries for a given table.
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchAllQueries();

	/**
	 * Returns history queries for a given table and date range.
	 *
	 * @param start - the start range date
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchAllQueriesFrom(Date start);

	/**
	 * Returns metadata for a given table.
	 *
	 * @param projectId
	 * @param datasetName
	 * @param tableName
	 * @return - FetchedTable of targeted table
	 */
	FetchedTable fetchTable(String projectId, String datasetName, String tableName);

	/**
	 * Returns all projects id.
	 *
	 * @return
	 */
	List<String> fetchProjectIds();


	/**
	 * Returns all the information for a given project.
	 *
	 * @return
	 */
	FetchedProject fetchProject(String projectId);

	/**
	 * Returns all dataset id in a given projectId.
	 *
	 * @return
	 */
	List<FetchedDataset> fetchAllDatasets();

	/**
	 * Returns the dataset metadata in a given projectId.
	 *
	 * @param datasetName
	 * @return
	 */
	FetchedDataset fetchDataset(String datasetName);

	List<FetchedTable> fetchAllTables();

	List<FetchedTable> fetchTablesInDataset(String datasetName);

}
