package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stripe.param.issuing.CardListParams.Status;

public class ProjectSubscription {

  @JsonProperty("id")
  private String subscriptionId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("priceId")
  private String priceId;

  public ProjectSubscription(String subscriptionId, String status, String priceId) {
    this.subscriptionId = subscriptionId;
    this.status = status;
    this.priceId = priceId;
  }

  public boolean isActive() {
    return this.status.equals(Status.ACTIVE.getValue());
  }

  public String getStatus() {
    return this.status;
  }

  public String getId() {
    return this.subscriptionId;
  }

  public String getPriceId() {
    return priceId;
  }
}
