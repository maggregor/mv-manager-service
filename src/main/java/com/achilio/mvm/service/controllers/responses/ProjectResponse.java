package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("projectName")
  private final String projectName;

  @JsonProperty("activated")
  private final Boolean activated;

  @JsonProperty("automatic")
  private final Boolean automatic;

  @JsonProperty("username")
  private final String username;

  public ProjectResponse(
      String projectId, String projectName, Boolean activated, Boolean automatic, String username) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.activated = activated;
    this.automatic = automatic;
    this.username = username;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getUsername() {
    return username;
  }
}
