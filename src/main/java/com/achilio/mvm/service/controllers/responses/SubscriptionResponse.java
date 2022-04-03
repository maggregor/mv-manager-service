package com.achilio.mvm.service.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItemCollection;

public class SubscriptionResponse {

  @JsonProperty private String status;
  @JsonProperty private Long trialEnd;
  @JsonProperty private SubscriptionItemCollection items;
  @JsonProperty private Long canceledAt;

  public SubscriptionResponse(Subscription subscription) {
    this.status = subscription.getStatus();
    this.trialEnd = subscription.getTrialEnd();
    this.items = subscription.getItems();
    this.canceledAt = subscription.getCanceledAt();
  }
}
