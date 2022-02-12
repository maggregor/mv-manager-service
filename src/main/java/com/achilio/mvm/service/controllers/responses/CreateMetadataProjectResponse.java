package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateMetadataProjectResponse {

  @JsonProperty private String projectId;

  @JsonProperty("path")
  private String resourcePath;

  public CreateMetadataProjectResponse(String projectId) {
    this.projectId = projectId;
    this.resourcePath = buildResourcePath(projectId);
  }

  public String getProjectId() {
    return projectId;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  private String buildResourcePath(String projectId) {
    return "/api/v1/project/" + projectId + "/metadata";
  }
}
