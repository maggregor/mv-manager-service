package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A Stripe product as a project plan Hold available pricing on this plan. Link to the current
 * subscription.
 */
public class ProjectPlan {

  private String name;
  // Reference to the stripe product
  @JsonProperty("id")
  private String stripeProductId;

  private List<ProjectPlanPrice> prices;

  private ProjectSubscription subscription;

  private String urlImage;

  private String description;

  public ProjectPlan(String name, String stripeProductId, String urlImage, String description) {
    this.name = name;
    this.stripeProductId = stripeProductId;
    this.urlImage = urlImage;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getStripeProductId() {
    return stripeProductId;
  }

  public List<ProjectPlanPrice> getPrices() {
    return prices;
  }

  public ProjectSubscription getSubscription() {
    return subscription;
  }

  public void setSubscription(ProjectSubscription subscription) {
    this.subscription = subscription;
  }

  public String getUrlImage() {
    return urlImage;
  }

  public String getDescription() {
    return description;
  }

  public boolean isEnabled() {
    return subscription != null && subscription.isActive();
  }

  public void setPricing(List<ProjectPlanPrice> prices) {
    this.prices = prices;
  }
}
