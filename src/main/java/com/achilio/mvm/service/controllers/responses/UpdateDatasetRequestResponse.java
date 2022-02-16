package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDatasetRequestResponse {

  @JsonProperty("activated")
  private boolean activated = false;

  public UpdateDatasetRequestResponse() {}

  public UpdateDatasetRequestResponse(boolean activated) {
    this.activated = activated;
  }

  public boolean isActivated() {
    return activated;
  }
}
