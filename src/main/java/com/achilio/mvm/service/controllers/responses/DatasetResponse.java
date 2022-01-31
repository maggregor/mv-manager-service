package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("datasetName")
  private final String datasetName;

  @JsonProperty("location")
  private final String location;

  @JsonProperty("friendlyName")
  private final String friendlyName;

  @JsonProperty("description")
  private final String description;

  @JsonProperty("createdAt")
  private final Long createdAt;

  @JsonProperty("lastModified")
  private final Long lastModified;

  public DatasetResponse(
      String projectId,
      String datasetName,
      String location,
      String friendlyName,
      String description,
      Long createdAt,
      Long lastModified) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.location = location;
    this.friendlyName = friendlyName;
    this.description = description;
    this.createdAt = createdAt;
    this.lastModified = lastModified;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public String getLocation() {
    return location;
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  public String getDescription() {
    return description;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Long getLastModified() {
    return lastModified;
  }
}
