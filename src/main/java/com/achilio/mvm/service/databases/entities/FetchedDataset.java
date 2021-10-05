package com.achilio.mvm.service.databases.entities;

/**
 * Represents a single project from database with metadata.
 */
public interface FetchedDataset {

	/**
	 * Return the project name.
	 *
	 * @return the project name.
	 */
	String getProjectId();

	/**
	 * Return the dataset name.
	 *
	 * @return the dataset name.
	 */
	String getDatasetName();

	String getLocation();

	String getFriendlyName();

	String getDescription();

	Long getCreatedAt();

	Long getLastModified();
}
