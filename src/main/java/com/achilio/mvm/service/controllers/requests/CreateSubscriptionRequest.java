package com.achilio.mvm.service.controllers.requests;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CreateSubscriptionRequest {

  private String customerId;
  private String priceId;
  private String projectId;

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
