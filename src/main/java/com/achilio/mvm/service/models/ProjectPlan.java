package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Project plan represent a stripe product the current project is subscribed to */
public class ProjectPlan {

  private final String name;
  private final String urlImage;
  private final String description;

  @JsonProperty("id")
  // Reference to the stripe product
  private final String stripeProductId;

  private List<PlanPrice> prices;
  private ProjectSubscription subscription;
  private PossibleAction possibleAction;

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

  public List<PlanPrice> getPrices() {
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

  public void setPricing(List<PlanPrice> prices) {
    this.prices = prices;
  }

  public void setPossibleAction(PossibleAction possibleAction) {
    this.possibleAction = possibleAction;
  }

  public enum PossibleAction {
    UPGRADE,
    DOWNGRADE,
    CANCEL,
    SUBSCRIBE;
  }
}
