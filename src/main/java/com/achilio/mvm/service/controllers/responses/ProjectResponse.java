package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.org.apache.xpath.internal.operations.Bool;

public class ProjectResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("projectName")
  private final String projectName;

  @JsonProperty("activated")
  private final Boolean activated;

  @JsonProperty("automatic")
  private final Boolean automatic;

  public ProjectResponse(String projectId, String projectName, Boolean activated, Boolean automatic) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.activated = activated;
    this.automatic = automatic;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }
}
