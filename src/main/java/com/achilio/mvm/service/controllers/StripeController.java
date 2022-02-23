package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.CreateSubscriptionRequest;
import com.achilio.mvm.service.models.ProjectPlan;
import com.achilio.mvm.service.models.ProjectSubscription;
import com.achilio.mvm.service.services.StripeService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class StripeController {

  private static Logger LOGGER = LoggerFactory.getLogger(StripeController.class);
  @Autowired private StripeService stripeService;

  @Value("${stripe.endpoint.secret}")
  private String endpointSecret;

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

  @PostMapping(path = "/webhook")
  @ApiOperation("Receive Stripe events")
  public void receiveStripeWebhook(
      @RequestHeader("Stripe-Signature") String header, @RequestBody String body)
      throws StripeException {
    Event event = null;
    try {
      event = Webhook.constructEvent(body, header, endpointSecret);
    } catch (SignatureVerificationException e) {
      // Invalid signature
      LOGGER.error("Invalid signature");
    }

    // Deserialize the nested object inside the event
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    StripeObject stripeObject;
    if (dataObjectDeserializer.getObject().isPresent()) {
      stripeObject = dataObjectDeserializer.getObject().get();
    } else {
      // Deserialization failed, probably due to an API version mismatch.
      // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
      // instructions on how to handle this case, or return an error here.
      LOGGER.error("Deserialization failed");
      throw new EventDataObjectDeserializationException("Deserialization failed", body);
    }

    Subscription subscription = (Subscription) stripeObject;
    String customerId = subscription.getCustomer();

    switch (event.getType()) {
      case "customer.subscription.deleted":
        // handle subscription canceled automatically based
        // upon your subscription settings. Or if the user
        // cancels it.
        LOGGER.info("Processing the deletion of the subscription");
      case "customer.subscription.updated":
        // handle subscription updated by updating the project
        // associated to its customerId
        LOGGER.info("Updating subscription for customer: {}", customerId);
      case "customer.subscription.created":
        LOGGER.info(
            "Processing the subscription {} for customer: {}",
            subscription.getStatus(),
            customerId);
        stripeService.handleSubscription(subscription, customerId);
        break;
      default:
        // Unhandled event type
        LOGGER.warn("Unhandled event type: {}", event.getType());
    }
  }
}
