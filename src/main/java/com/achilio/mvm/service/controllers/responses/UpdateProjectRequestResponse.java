package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProjectRequestResponse {

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("analysisTimeframe")
  private Integer analysisTimeframe;

  @JsonProperty("mvMaxPerTable")
  private Integer mvMaxPerTable;

  public UpdateProjectRequestResponse(
      Boolean automatic, Integer analysisTimeframe, Integer mvMaxPerTable) {
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
