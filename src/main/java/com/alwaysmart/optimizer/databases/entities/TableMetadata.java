package com.alwaysmart.optimizer.databases.entities;

import java.util.Map;

/**
 * Represents a single table from database with metadata.
 */
public interface TableMetadata {

	/**
	 * The table id of the table on which metadata was retrieved.
	 * Example: {@code project.dataset:mytable}
	 *
	 * @return the table id of the table on which metadata was retrieved.
	 */
	String getTableId();

	/**
	 * The project name of the table on which metadata was retrieved.
	 *
	 * @return the project name of the table on which metadata was retrieved.
	 * */
	String getProject();

	/**
	 * The schema dataset of the table on which metadata was retrieved.
	 * Example: {@code default}
	 *
	 * @return the dataset name of the table on which metadata was retrieved.
	 */
	String getDataset();

	/**
	 * The name of the table on which metadata was retrieved.
	 * Example: {@code my_table}
	 *
	 * @return the name of the table on which metadata was retrieved.
	 */
	String getTable();

	/**
	 * The name and type of the columns of the table.
	 * Example: {@code [{col1, STRING}, {col2, INTEGER}]
	 *
	 * @return the name and type of the columns of the table.
	 */
	Map<String, String> getColumns();

}
