package com.alwaysmart.optimizer;

import java.util.Date;
import java.util.List;

/**
 * DataWarehouse fetcher interface
 */
public interface DataWarehouseFetcher {

	/**
	 * Returns history queries for a given dataset.
	 *
	 * @param dataset - the targeted dataset
	 *
	 * @return - a list of queries as string
	 */
	default List<FetchedQuery> fetchQueries(String dataset) {
		throw new UnsupportedOperationException("Not supported to fetch all tables in dataset.");
	}

	/**
	 * Returns history queries for a given table.
	 *
	 * @param dataset - the targeted dataset
	 * @param table - the targeted table
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String dataset, String table);

	/**
	 * Returns history queries for a given table and date range.
	 *
	 * @param dataset - the targeted dataset
	 * @param table - the targeted table
	 * @param start - the start range date
	 * @param end - the end range date
	 *
	 * @return - a list of queries as string
	 */
	List<FetchedQuery> fetchQueries(String dataset, String table, Date start, Date end);

	/**
	 * Returns all metadata of all tables in a given dataset.
	 *
	 * @param dataset - the targeted dataset
	 *
	 * @return - all metadata of fetched tables
	 */
	default List<TableMetadata> fetchTablesMetadata(String dataset) {
		throw new UnsupportedOperationException("Not supported to fetch all tables in dataset.");
	}

	/**
	 * Returns metadata for a given table.
	 *
	 * @param dataset - the targeted dataset
	 * @param table - the targeted tableFe
	 *
	 * @return - TableMetadata of targeted table
	 */
	TableMetadata fetchTableMetadata(String dataset, String table);

}
