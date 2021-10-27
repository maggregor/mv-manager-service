package com.achilio.mvm.service.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("projectName")
  private final String projectName;

  @JsonProperty("activated")
  private final boolean activated;

  public ProjectResponse(String projectId, String projectName, boolean activated) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.activated = activated;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }
}
