package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProjectRequestResponse {

  @JsonProperty("planName")
  private final String planName;

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("username")
  private String username;

  @JsonProperty("analysisTimeframe")
  private Integer analysisTimeframe;

  @JsonProperty("mvMaxPerTable")
  private Integer mvMaxPerTable;

  public UpdateProjectRequestResponse(
      String planName,
      Boolean automatic,
      String username,
      Integer analysisTimeframe,
      Integer mvMaxPerTable) {
    this.planName = planName;
    this.automatic = automatic;
    this.username = username;
    this.analysisTimeframe = analysisTimeframe;
    this.mvMaxPerTable = mvMaxPerTable;
  }

  public String getPlanName() {
    return planName;
  }

  public Boolean isAutomatic() {
    return automatic;
  }

  public String getUsername() {
    return username;
  }

  public Integer getAnalysisTimeframe() {
    return analysisTimeframe;
  }

  public Integer getMvMaxPerTable() {
    return mvMaxPerTable;
  }
}
