package com.achilio.mvm.service.controllers.requests;

public class CreateSubscriptionRequest {

  private String customerId;
  private String priceId;
  private String projectId;

  public CreateSubscriptionRequest() {}

  public String getCustomerId() {
    return this.customerId;
  }

  public String getPriceId() {
    return this.priceId;
  }

  public String getProjectId() {
    return this.projectId;
  }
}
