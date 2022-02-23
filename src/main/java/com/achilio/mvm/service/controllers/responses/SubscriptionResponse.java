package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionResponse {

  @JsonProperty("productId")
  private String productId;

  @JsonProperty("priceId")
  private String priceId;

  @JsonProperty("active")
  private boolean active;

  public SubscriptionResponse(String productId, String priceId, boolean active) {
    this.productId = productId;
    this.priceId = priceId;
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }
}
