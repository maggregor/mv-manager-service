package com.alwaysmart.optimizer.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProjectResponse {

	@JsonProperty("projectId")
	private final String projectId;

	@JsonProperty("plan")
	private final String plan;

	@JsonProperty("datasets")
	private final List<String> datasets;

	@JsonProperty("activated")
	private final boolean activated;

	public ProjectResponse(String projectId,
						   String plan,
						   boolean activated,
						   List<String> datasets) {
		this.projectId = projectId;
		this.plan = plan;
		this.activated = activated;
		this.datasets = datasets;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getPlan() {
		return plan;
	}

	public boolean isActivated() {
		return activated;
	}

	public List<String> getDatasets() {
		return datasets;
	}

}
