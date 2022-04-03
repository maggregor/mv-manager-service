package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextStripeCustomerId;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.responses.SubscriptionResponse;
import com.achilio.mvm.service.services.StripeService;
import com.achilio.mvm.service.services.StripeService.SubscriptionCheck;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import io.swagger.annotations.ApiOperation;
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

  @GetMapping(path = "/create-customer-portal-session", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Create a Stripe Portal session to allow user to manage its subscription")
  public String createPortalSession() throws StripeException {
    return service.createPortalSession(getContextStripeCustomerId());
  }

  @GetMapping(path = "/subscription", produces = APPLICATION_JSON_VALUE)
  @ApiOperation(
      "Get the subscription of the current user's team.\n"
          + "Each team is supposed to have only one subscription")
  public SubscriptionResponse getSubscription() throws StripeException {
    return new SubscriptionResponse(service.getSubscription(getContextStripeCustomerId()));
  }

  @GetMapping(path = "/subscription/checks")
  @ApiOperation("Get list of subscriptions checks")
  public List<SubscriptionCheck> getSubscriptionChecks() throws StripeException {
    return service.subscriptionChecks(getContextStripeCustomerId());
  }

  @GetMapping(path = "/products")
  @ApiOperation("Get all Achilio products from Stripe")
  public List<Product> getAllProducts() throws StripeException {
    return service.getAllProducts();
  }

  @GetMapping(path = "/prices")
  @ApiOperation("Get all prices for the main Achilio product")
  public List<Price> getAllPrices() throws StripeException {
    return service.getAllPrices();
  }
}
