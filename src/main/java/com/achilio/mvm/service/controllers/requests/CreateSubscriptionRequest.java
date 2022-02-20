package com.achilio.mvm.service.controllers.requests;

public class CreateSubscriptionRequest {

  private String stripeCustomerId;
  private String priceId;

  public CreateSubscriptionRequest() {}

  public String getStripeCustomerId() {
    return this.stripeCustomerId;
  }

  public String getPriceId() {
    return this.priceId;
  }
}
