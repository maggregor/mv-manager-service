package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.CreateSubscriptionRequest;
import com.achilio.mvm.service.controllers.responses.SubscriptionResponse;
import com.achilio.mvm.service.services.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class StripeController {

  final String API_KEY =
      "sk_test_51I8oL1Kz3TV8XBbdgPtkPKW1Mh3qrWfC13TbJBUQND9rWNvtyTT3wVssVYbgDteA5xTpzE5XEM9IfAXWQLZZJWPw00ljma7GOR";
  @Autowired private StripeService stripeService;

  @GetMapping(path = "/subscription/products", produces = "application/json")
  public List<Product> getAllProducts() throws StripeException {
    Stripe.apiKey = API_KEY;
    return stripeService.getAllProducts();
  }

  @GetMapping(path = "/subscription/prices", produces = "application/json")
  public List<Price> getAllPrices() throws StripeException {
    Stripe.apiKey = API_KEY;
    return stripeService.getAllPrices();
  }

  @PostMapping(path = "/subscription", produces = "application/json")
  public Map<String, Object> createSubscription(@RequestBody CreateSubscriptionRequest request)
      throws StripeException {
    return stripeService.createSubscription(request.getStripeCustomerId(), request.getPriceId());
  }

  @GetMapping(path = "/subscription", produces = "application/json")
  public Map<String, Object> getSubscription(HttpServletRequest request) throws StripeException {
    Map<String, Object> response = new HashMap<>();
    Subscription subscription =
        stripeService.getSubscription(request.getParameter("stripeCustomerId"));
    if (subscription == null) {
      response.put("active", false);
    } else {
      SubscriptionResponse subscriptionResponse = toResponse(subscription);
      response.put("active", subscriptionResponse.isActive());
      response.put("subscription", subscriptionResponse);
    }
    return response;
  }

  public SubscriptionResponse toResponse(Subscription subscription) {
    String productId = null;
    String priceId = null;
    boolean active = subscription.getStatus().equalsIgnoreCase("active");
    List<SubscriptionItem> items = subscription.getItems().getData();
    if (!items.isEmpty()) {
      Price price = items.get(0).getPrice();
      productId = price.getProduct();
      priceId = price.getId();
    }
    return new SubscriptionResponse(productId, priceId, active);
  }
}
