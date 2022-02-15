package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateMetadataProjectRequestResponse {

  @JsonProperty("planName")
  private final String planName;

  @JsonProperty("activated")
  private final Boolean activated;

  @JsonProperty("automatic")
  private Boolean automatic;

  public UpdateMetadataProjectRequestResponse(
      String planName, Boolean activated, Boolean automatic) {
    this.planName = planName;
    this.activated = activated;
    this.automatic = automatic;
  }

  public String getPlanName() {
    return planName;
  }

  public Boolean isActivated() {
    return activated;
  }

  public Boolean isAutomatic() {
    return automatic;
  }

  public void setAutomatic(Boolean automatic) {
    this.automatic = automatic;
  }
}
