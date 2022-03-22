package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.ADataset;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDatasetRequestResponse {

  @JsonProperty("name")
  private String name;

  @JsonProperty("activated")
  private boolean activated = false;

  public UpdateDatasetRequestResponse() {}

  public UpdateDatasetRequestResponse(ADataset dataset) {
    new UpdateDatasetRequestResponse(dataset.getDatasetName(), dataset.isActivated());
  }

  public UpdateDatasetRequestResponse(String name, boolean activated) {
    this.name = name;
    this.activated = activated;
  }

  public boolean isActivated() {
    return activated;
  }

  public String getName() {
    return name;
  }
}
