package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextStripeCustomerId;

import com.achilio.mvm.service.services.StripeService;
import com.achilio.mvm.service.services.StripeService.SubscriptionCheck;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class StripeController {

  private final StripeService service;

  public StripeController(StripeService service) {
    this.service = service;
  }

  @GetMapping(path = "/create-customer-portal-session")
  public String createPortalSession() throws StripeException {
    return service.createPortalSession(getContextStripeCustomerId());
  }

  @GetMapping(path = "/subscription")
  public Subscription getSubscription() throws StripeException {
    return service.getSubscription(getContextStripeCustomerId());
  }

  @GetMapping(path = "/subscription/checks")
  public List<SubscriptionCheck> getSubscriptionChecks() throws StripeException {
    return service.subscriptionChecks(getContextStripeCustomerId());
  }

  @GetMapping(path = "/products")
  public List<Product> getAllProducts() throws StripeException {
    return service.getAllProducts();
  }

  @GetMapping(path = "/prices")
  public List<Price> getAllPrices() throws StripeException {
    return service.getAllPrices();
  }
}
