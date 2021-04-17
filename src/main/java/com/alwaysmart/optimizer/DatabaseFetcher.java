package com.alwaysmart.optimizer;

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
	 * @return - TableMetadata of targeted table
	 */
	TableMetadata fetchTable(String tableId);

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
	ProjectMetadata fetchProject(String projectName);

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
	DatasetMetadata fetchDataset(String datasetId);

	/**
	 * Returns all tables id in a given dataset.
	 *
	 * @param datasetId
	 * @return
	 */
	List<String> fetchTableIds(String datasetId);

	/**
	 * #TODO -> Dirty
	 * Returns table metadata for each given table.
	 *
	 * @param tableIds - List of TableId object.
	 * @return the table metadata for each given table.
	 */
	default List<TableMetadata> fetchTables(Collection<TableId> tableIds) {
		List<TableMetadata> tables = new ArrayList<>();
		for (TableId tableId : tableIds) {
			TableMetadata table = fetchTable(BigQueryHelper.tableToString(tableId));
			if (table != null) {
				tables.add(table);
			}
		}
		return tables;
	}

}
