package com.alwaysmart.optimizer.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableResponse {

	@JsonProperty("projectId")
	private final String projectId;

	@JsonProperty("datasetName")
	private final String datasetName;

	@JsonProperty("tableName")
	private final String tableName;

	public TableResponse(final String projectId,
						 final String datasetName,
						 final String tableName) {
		this.projectId = projectId;
		this.datasetName = datasetName;
		this.tableName = tableName;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public String getTableName() {
		return tableName;
	}

}
