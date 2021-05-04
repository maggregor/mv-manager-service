package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

/**
 * Represents a single project from database with metadata.
 */
public interface FetchedDataset {

	/**
	 * Return the dataset id.
	 *
	 * @return the dataset id.
	 */
	String getDatasetId();

	/**
	 * Return the project name.
	 *
	 * @return the project name.
	 */
	String getProject();

	/**
	 * Return the dataset name.
	 *
	 * @return the dataset name.
	 */
	String getName();

	/**
	 * Return the table id list.
	 *
	 * @return the table id list.
	 */
	List<String> getTables();

}
