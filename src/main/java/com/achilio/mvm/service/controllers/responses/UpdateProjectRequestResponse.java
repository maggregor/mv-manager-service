package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProjectRequestResponse {

  @JsonProperty("planName")
  private final String planName;

  @JsonProperty("activated")
  private final Boolean activated;

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("username")
  private String username;

  public UpdateProjectRequestResponse(
      String planName, Boolean activated, Boolean automatic, String username) {
    this.planName = planName;
    this.activated = activated;
    this.automatic = automatic;
    this.username = username;
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

  public String getUsername() {
    return username;
  }
}