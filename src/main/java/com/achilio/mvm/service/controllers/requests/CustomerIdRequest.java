package com.achilio.mvm.service.controllers.requests;

public class CustomerIdRequest {

  private String stripeCustomerId;

  public CustomerIdRequest() {}

  public String getStripeCustomerId() {
    return this.stripeCustomerId;
  }
}
