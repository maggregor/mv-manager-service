package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represent a stripe price object */
public class PlanPrice {

  // ie: in cent 9990 for 99.90
  private final Long amount;
  // ie: usd, eur
  private final String currency;
  // ie: month, year
  private final String interval;
  @JsonProperty("id")
  private String stripePriceId;

  public PlanPrice(String stripePriceId, Long amount, String currency, String interval) {
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
