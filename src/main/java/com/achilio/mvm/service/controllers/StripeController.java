package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.CreateCheckoutRequest;
import com.achilio.mvm.service.services.ProjectService;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
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
  final String YOUR_DOMAIN = "http://localhost:8081";
  // Replace this endpoint secret with your endpoint's unique secret
  // If you are testing with the CLI, find the secret by running 'stripe listen'
  // If you are using an endpoint defined with the API or dashboard, look in your webhook settings
  // at https://dashboard.stripe.com/webhooks
  String endpointSecret = "";
  @Autowired private ProjectService service;

  @PostMapping(path = "/stripe/{projectId}/create-checkout-session", produces = "application/json")
  @ApiOperation("")
  public String createCheckoutSession(
      @PathVariable String projectId,
      HttpServletResponse response,
      @RequestBody CreateCheckoutRequest payload)
      throws StripeException {
    String url = String.format("%s/api/v1/projects/%s/subscription", YOUR_DOMAIN, projectId);
    Stripe.apiKey = API_KEY;
    Price price = Price.retrieve(payload.getPriceLookUpKey());
    SessionCreateParams params =
        SessionCreateParams.builder()
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(price.getId())
                    .setQuantity(1L)
                    .build())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(url + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(url)
            .build();
    return Session.create(params).getUrl();
  }

  @PostMapping(path = "/stripe/{projectId}/create-portal-session")
  @ApiOperation("")
  public String createPortalSession(HttpServletRequest request, HttpServletResponse response)
      throws StripeException {
    Session checkoutSession = Session.retrieve(request.getParameter("session_id"));

    String customer = checkoutSession.getCustomer();
    // Authenticate your user.
    com.stripe.param.billingportal.SessionCreateParams params =
        new com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(YOUR_DOMAIN)
            .setCustomer(customer)
            .build();

    com.stripe.model.billingportal.Session portalSession =
        com.stripe.model.billingportal.Session.create(params);

    response.setHeader("Location", portalSession.getUrl());
    response.setStatus(303);
    return "";
  }
  ;

  @PostMapping(path = "/stripe/webhook")
  @ApiOperation("")
  public String webhook(
      HttpServletRequest request, HttpServletResponse response, @RequestBody String body) {
    Event event = null;
    try {
      event = ApiResource.GSON.fromJson(body, Event.class);
    } catch (JsonSyntaxException e) {
      // Invalid payload
      System.out.println("⚠️  Webhook error while parsing basic request.");
      response.setStatus(400);
      return "";
    }
    String sigHeader = request.getHeader("Stripe-Signature");
    if (endpointSecret != null && sigHeader != null) {
      // Only verify the event if you have an endpoint secret defined.
      // Otherwise use the basic event deserialized with GSON.
      try {
        event = Webhook.constructEvent(body, sigHeader, endpointSecret);
      } catch (SignatureVerificationException e) {
        // Invalid signature
        System.out.println("⚠️  Webhook error while validating signature.");
        response.setStatus(400);
        return "";
      }
    }
    // Deserialize the nested object inside the event
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    StripeObject stripeObject = null;
    if (dataObjectDeserializer.getObject().isPresent()) {
      stripeObject = dataObjectDeserializer.getObject().get();
    } else {
      // Deserialization failed, probably due to an API version mismatch.
      // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
      // instructions on how to handle this case, or return an error here.
    }
    // Handle the event
    Subscription subscription = null;
    switch (event.getType()) {
      case "customer.subscription.deleted":
        subscription = (Subscription) stripeObject;
        // Then define and call a function to handle the event
        // customer.subscription.deleted
        // handleSubscriptionTrialEnding(subscription);
      case "customer.subscription.trial_will_end":
        subscription = (Subscription) stripeObject;
        // Then define and call a function to handle the event
        // customer.subscription.trial_will_end
        // handleSubscriptionDeleted(subscriptionDeleted);
      case "customer.subscription.created":
        subscription = (Subscription) stripeObject;
        // Then define and call a function to handle the event
        // customer.subscription.created
        // handleSubscriptionCreated(subscription);
      case "customer.subscription.updated":
        subscription = (Subscription) stripeObject;
        // Then define and call a function to handle the event
        // customer.subscription.updated
        // handleSubscriptionUpdated(subscription);
        // ... handle other event types
      default:
        System.out.println("Unhandled event type: " + event.getType());
    }
    response.setStatus(200);
    return "";
  }
  ;
}
