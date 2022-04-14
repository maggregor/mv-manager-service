package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProjectRequest {

  @JsonProperty("project_name")
  private String projectName;

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("analysisTimeframe")
  private Integer analysisTimeframe;

  public UpdateProjectRequest(String projectName, Boolean automatic, Integer analysisTimeframe) {
    this.projectName = projectName;
    this.automatic = automatic;
    this.analysisTimeframe = analysisTimeframe;
  }

  public Boolean isAutomatic() {
    return automatic;
  }

  public Integer getAnalysisTimeframe() {
    return analysisTimeframe;
  }

  public String getProjectName() {
    return projectName;
  }
}
