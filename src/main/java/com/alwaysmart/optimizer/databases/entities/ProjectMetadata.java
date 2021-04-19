package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

/**
 * Represents a single project from database with metadata.
 */
public interface ProjectMetadata {

	/**
	 * Return the project name.
	 *
	 * @return the project name.
	 */
	String getName();

	/**
	 * Return the dataset list.
	 *
	 * @return the dataset list.
	 */
	List<String> getDatasets();

}
