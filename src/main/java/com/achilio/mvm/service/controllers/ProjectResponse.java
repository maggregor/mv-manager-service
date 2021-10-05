package com.achilio.mvm.service.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectResponse {

	@JsonProperty("projectId")
	private final String projectId;

	@JsonProperty("projectName")
	private final String projectName;

	public ProjectResponse(String projectId, String projectName){
		this.projectId = projectId;
		this.projectName = projectName;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

}
