package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

public class DefaultFetchedProject implements FetchedProject {

	private String projectId;
	private String name;
	private List<String> datasets;

	public DefaultFetchedProject(String projectId, String name, List<String> datasets) {
		this.projectId = projectId;
		this.name = name;
		this.datasets = datasets;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<String> getDatasets() {
		return this.datasets;
	}
}
