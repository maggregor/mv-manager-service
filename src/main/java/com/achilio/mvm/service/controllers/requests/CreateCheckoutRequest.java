package com.achilio.mvm.service.controllers.requests;

public class CreateCheckoutRequest {

  private String priceLookUpKey;

  public CreateCheckoutRequest()  {

  }

  public CreateCheckoutRequest(String priceLookUpKey) {
    this.priceLookUpKey = priceLookUpKey;
  }

  public String getPriceLookUpKey() {
    return this.priceLookUpKey;
  }
}
