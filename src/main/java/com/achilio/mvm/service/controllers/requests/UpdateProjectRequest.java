package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProjectRequest {

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("analysisTimeframe")
  private Integer analysisTimeframe;

  @JsonProperty("mvMaxPerTable")
  private Integer mvMaxPerTable;

  public UpdateProjectRequest(Boolean automatic, Integer analysisTimeframe, Integer mvMaxPerTable) {
    this.automatic = automatic;
    this.analysisTimeframe = analysisTimeframe;
    this.mvMaxPerTable = mvMaxPerTable;
  }

  public Boolean isAutomatic() {
    return automatic;
  }

  public Integer getAnalysisTimeframe() {
    return analysisTimeframe;
  }

  public Integer getMvMaxPerTable() {
    return mvMaxPerTable;
  }
}
