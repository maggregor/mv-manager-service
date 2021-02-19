package com.alwaysmart.optimizer;

import java.util.Map;

/**
 * Represents a single table from database with metadata.
 */
public interface TableMetadata {

	/**
	 * The schema name of the table on which metadata was retrieved.
	 * Example: {@code default}
	 *
	 * @return the schema name of the table on which metadata was retrieved.
	 */
	String schema();

	/**
	 * The name of the table on which metadata was retrieved.
	 * Example: {@code my_table}
	 *
	 * @return the name of the table on which metadata was retrieved.
	 */
	String table();

	/**
	 * The name and type of the columns of the table.
	 * Example: {@code [{col1, STRING}, {col2, INTEGER}]
	 *
	 * @return the name and type of the columns of the table.
	 */
	Map<String, String> columns();

}
