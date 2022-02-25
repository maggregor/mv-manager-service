package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stripe.param.issuing.CardListParams.Status;

public class ProjectSubscription {

  @JsonProperty("id")
  private String subscriptionId;

  @JsonProperty("status")
  private String status;

  public ProjectSubscription(String subscriptionId, String status) {
    this.subscriptionId = subscriptionId;
    this.status = status;
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
}
