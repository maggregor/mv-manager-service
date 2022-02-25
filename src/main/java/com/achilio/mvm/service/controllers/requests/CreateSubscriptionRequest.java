package com.achilio.mvm.service.controllers.requests;

public class CreateSubscriptionRequest {

  private String projectId;
  private String priceId;

  public CreateSubscriptionRequest() {}

  public String getProjectId() {
    return this.projectId;
  }

  public String getPriceId() {
    return this.priceId;
  }
}
