package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectPlanPrice {

  @JsonProperty("id")
  private String stripePriceId;

  // ie: in cent 9990 for 99.90
  private Long amount;

  // ie: usd, eur
  private String currency;

  // ie: month, year
  private String interval;

  public ProjectPlanPrice(String stripePriceId, Long amount, String currency, String interval) {
    this.stripePriceId = stripePriceId;
    this.amount = amount;
    this.currency = currency;
    this.interval = interval;
  }

  public String getStripePriceId() {
    return stripePriceId;
  }

  public String getCurrency() {
    return this.currency;
  }

  public String getInterval() {
    return this.interval;
  }

  public Long getAmount() {
    return amount;
  }
}
