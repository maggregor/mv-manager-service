package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateMetadataProjectRequestResponse {

  @JsonProperty("planName")
  private final String planName;

  @JsonProperty("activated")
  private final boolean activated;

  public UpdateMetadataProjectRequestResponse(String planName, boolean activated) {
    this.planName = planName;
    this.activated = activated;
  }

  public String getPlanName() {
    return planName;
  }

  public boolean isActivated() {
    return activated;
  }
}
