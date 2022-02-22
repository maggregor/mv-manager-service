package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.CreateSubscriptionRequest;
import com.achilio.mvm.service.models.ProjectPlan;
import com.achilio.mvm.service.models.ProjectSubscription;
import com.achilio.mvm.service.services.StripeService;
import com.stripe.exception.StripeException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class StripeController {

  @Autowired private StripeService stripeService;

  @GetMapping(path = "/plan", produces = "application/json")
  public List<ProjectPlan> getProjectPlans(@RequestParam String customerId) throws StripeException {
    return stripeService.getPlans(customerId);
  }

  @PostMapping(path = "/subscription", produces = "application/json")
  public ProjectSubscription createSubscription(@RequestBody CreateSubscriptionRequest request)
      throws StripeException {
    return stripeService.createSubscription(request.getCustomerId(), request.getPriceId());
  }

  @GetMapping(path = "/subscription/{subscriptionId}", produces = "application/json")
  public ProjectSubscription getSubscription(@PathVariable String subscriptionId)
      throws StripeException {
    return stripeService.getSubscription(subscriptionId);
  }

  @GetMapping(
      path = "/subscription/{subscriptionId}/latestIntentClientSecret",
      produces = "application/json")
  public String getLatestIntentClientSecret(@PathVariable String subscriptionId)
      throws StripeException {
    return stripeService.getLatestIntentClientSecret(subscriptionId);
  }

  @DeleteMapping(path = "/subscription/{subscriptionId}", produces = "application/json")
  public ProjectSubscription cancelSubscription(@PathVariable String subscriptionId)
      throws StripeException {
    return stripeService.cancelSubscription(subscriptionId);
  }

  @PostMapping(path = "/subscription/{subscriptionId}", produces = "application/json")
  public ProjectSubscription updateSubscription(
      @PathVariable String subscriptionId, UpdateSubscriptionRequest request)
      throws StripeException {
    ProjectSubscription subscription = stripeService.getSubscription(subscriptionId);
    if (request.getPriceId() != null) {
      subscription = stripeService.changeSubscriptionPricing(subscriptionId, request.getPriceId());
    }
    return subscription;
  }
}
