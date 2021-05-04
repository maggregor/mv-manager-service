package com.alwaysmart.optimizer.databases;

import com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper;
import com.alwaysmart.optimizer.databases.entities.FetchedDataset;
import com.alwaysmart.optimizer.databases.entities.FetchedProject;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.google.cloud.bigquery.TableId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Database fetcher interface
 */
public interface DatabaseFetcher {

	/**
	 * Returns history queries for a given table.
	 *
	 * @param tableId - the targeted table id
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String tableId);

	/**
	 * Returns history queries for a given table and date range.
	 *
	 * @param tableId - the targeted project id
	 * @param start       - the start range date
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String tableId, Date start);

	/**
	 * Returns metadata for a given table.
	 *
	 * @param tableId   - the targeted table id
	 *
	 * @return - FetchedTable of targeted table
	 */
	FetchedTable fetchTable(String tableId);

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
	FetchedProject fetchProject(String projectName);

	/**
	 * Returns all dataset id in a given projectId.
	 *
	 * @param projectId
	 * @return
	 */
	List<String> fetchDatasetIds(String projectId);

	/**
	 * Returns the dataset metadata in a given projectId.
	 *
	 * @param datasetId
	 * @return
	 */
	FetchedDataset fetchDataset(String datasetId);

	/**
	 * Returns all tables id in a given dataset.
	 *
	 * @param datasetId
	 * @return
	 */
	List<String> fetchTableIds(String datasetId);

	/**
	 * #TODO -> Dirty with this tableToString
	 * Returns table metadata for each given table.
	 *
	 * @param tableIds - List of TableId object.
	 * @return the table metadata for each given table.
	 */
	default List<FetchedTable> fetchTables(Collection<TableId> tableIds) {
		List<FetchedTable> tables = new ArrayList<>();
		for (TableId tableId : tableIds) {
			FetchedTable table = fetchTable(BigQueryHelper.tableToString(tableId));
			if (table != null) {
				tables.add(table);
			}
		}
		return tables;
	}

}
