package com.alwaysmart.optimizer.databases.entities;

import java.util.List;

public class DefaultFetchedProject implements FetchedProject {

	private String projectId;
	private String name;

	public DefaultFetchedProject(String projectId, String name) {
		this.projectId = projectId;
		this.name = name;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
