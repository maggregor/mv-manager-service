package com.alwaysmart.optimizer.databases.entities;

import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.google.cloud.bigquery.TableId;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public interface FetchedQuery {


	/**
	 * The TableId origin of the SELECT.
	 *
	 * @return the TableId origin of the SELECT.
	 */
	TableId getTableId();

	/**
	 * Define the TableId origin.
	 *
	 * @param tableId
	 */
	void setTableId(TableId tableId);

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
