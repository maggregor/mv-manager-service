package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

public class DefaultProjectMetadata implements ProjectMetadata {

	private String name;
	private List<String> datasets;

	public DefaultProjectMetadata(String name, List<String> datasets) {
		this.name = name;
		this.datasets = datasets;
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
