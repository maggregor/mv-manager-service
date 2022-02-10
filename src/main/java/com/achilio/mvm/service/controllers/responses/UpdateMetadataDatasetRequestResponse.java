package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateMetadataDatasetRequestResponse {

  @JsonProperty("activated")
  private boolean activated = false;

  public UpdateMetadataDatasetRequestResponse() {

  }

  public UpdateMetadataDatasetRequestResponse(boolean activated) {
    this.activated = activated;
  }

  public boolean isActivated() {
    return activated;
  }
}
