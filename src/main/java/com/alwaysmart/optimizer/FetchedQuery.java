package com.alwaysmart.optimizer;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public interface FetchedQuery {

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
