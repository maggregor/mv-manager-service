package com.alwaysmart.optimizer.databases.entities;

public class DefaultFetchedDataset implements FetchedDataset {

	private final String projectId;
	private final String datasetName;
	private final String location;
	private final String friendlyName;
	private final String description;
	private final Long createdAt;
	private final Long lastModified;

	public DefaultFetchedDataset(final String projectId,
								 final String datasetName,
								 final String location,
								 final String friendlyName,
								 final String description,
								 final Long createdAt,
								 final Long lastModified) {
		this.projectId = projectId;
		this.datasetName = datasetName;
		this.location = location;
		this.friendlyName = friendlyName;
		this.description = description;
		this.createdAt = createdAt;
		this.lastModified = lastModified;
	}

	@Override
	public String getDatasetName() {
		return this.datasetName;
	}

	@Override
	public String getProjectId() {
		return this.projectId;
	}

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public String getFriendlyName() {
		return friendlyName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Long getCreatedAt() {
		return createdAt;
	}

	@Override
	public Long getLastModified() {
		return lastModified;
	}

}
