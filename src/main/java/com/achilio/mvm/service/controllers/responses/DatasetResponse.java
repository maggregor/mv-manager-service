package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("datasetName")
  private final String datasetName;

  @JsonProperty("location")
  @JsonInclude(Include.NON_NULL)
  private final String location;

  @JsonProperty("friendlyName")
  @JsonInclude(Include.NON_NULL)
  private final String friendlyName;

  @JsonProperty("description")
  @JsonInclude(Include.NON_NULL)
  private final String description;

  @JsonProperty("createdAt")
  @JsonInclude(Include.NON_NULL)
  private final Long createdAt;

  @JsonProperty("lastModified")
  @JsonInclude(Include.NON_NULL)
  private final Long lastModified;

  @JsonProperty("activated")
  private final Boolean activated;

  public DatasetResponse(
      String projectId,
      String datasetName,
      String location,
      String friendlyName,
      String description,
      Long createdAt,
      Long lastModified,
      Boolean activated) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.location = location;
    this.friendlyName = friendlyName;
    this.description = description;
    this.createdAt = createdAt;
    this.lastModified = lastModified;
    this.activated = activated;
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

  public Boolean isActivated() {
    return this.activated;
  }
}
