package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

public class DefaultDatasetMetadata implements DatasetMetadata {

	private String datasetId;
	private String project;
	private String name;
	private List<String> tables;

	public DefaultDatasetMetadata(String datasetId, String project, String name, List<String> tables) {
		this.datasetId = datasetId;
		this.project = project;
		this.name = name;
		this.tables = tables;
	}

	@Override
	public String getDatasetId() {
		return this.datasetId;
	}

	@Override
	public String getProject() {
		return this.project;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<String> getTables() {
		return this.tables;
	}
}
