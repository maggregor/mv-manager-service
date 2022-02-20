package com.achilio.mvm.service.services;

import com.google.api.services.oauth2.model.Userinfo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  private static Logger LOGGER = LoggerFactory.getLogger(StripeService.class);
  final String API_KEY =
      "sk_test_51I8oL1Kz3TV8XBbdgPtkPKW1Mh3qrWfC13TbJBUQND9rWNvtyTT3wVssVYbgDteA5xTpzE5XEM9IfAXWQLZZJWPw00ljma7GOR";
  @Autowired private FetcherService fetcherService;

  /**
   * Create a Stripe customer
   *
   * @return the stripe customer ID
   */
  public String createCustomer(String projectId) {
    Stripe.apiKey = API_KEY;
    Userinfo userinfo;
    try {
      userinfo = fetcherService.getUserInfo();
      final String createdByMail = userinfo.getEmail();
      final String createdByUserId = userinfo.getId();
      final String createdByName = userinfo.getName();
      Map<String, String> metadata = new HashMap<>();
      metadata.put(CustomerMetadata.PROJECT_ID.toString(), projectId);
      metadata.put(CustomerMetadata.CREATED_BY_EMAIL.toString(), createdByMail);
      metadata.put(CustomerMetadata.CREATED_BY_USER_ID.toString(), createdByUserId);
      metadata.put(CustomerMetadata.CREATED_BY_NAME.toString(), createdByName);
      CustomerCreateParams params =
          CustomerCreateParams.builder()
              .setMetadata(metadata)
              .setEmail(createdByMail)
              .setName(projectId)
              .build();
      LOGGER.debug("Creating customer for {}", createdByMail);
      return Customer.create(params).getId();
    } catch (IOException e) {
      LOGGER.error("Error while retrieving user info ", e);
    } catch (StripeException e) {
      LOGGER.error("Error while creating the stripe customer for project {}", projectId, e);
    }
    return null;
  }

  public List<Product> getAllProducts() throws StripeException {
    Stripe.apiKey = API_KEY;
    ProductCollection productCollection = Product.list(ProductListParams.builder().build());
    return productCollection.getData();
  }

  public List<Price> getAllPrices() throws StripeException {
    Stripe.apiKey = API_KEY;
    return Price.list(PriceListParams.builder().build()).getData();
  }

  public Map<String, Object> createSubscription(String customerId, String priceId)
      throws StripeException {
    Stripe.apiKey = API_KEY;

    // Create the subscription. Note we're expanding the Subscription's
    // latest invoice and that invoice's payment_intent
    // so we can pass it to the front end to confirm the payment
    SubscriptionCreateParams subCreateParams =
        SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
            .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
            .build();

    Subscription subscription = Subscription.create(subCreateParams);

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("subscriptionId", subscription.getId());
    subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret();
    responseData.put(
        "clientSecret",
        subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret());
    return responseData;
  }

  public Subscription getSubscription(String stripeCustomerId) throws StripeException {
    Stripe.apiKey = API_KEY;
    List<Subscription> subscriptions =
        Subscription.list(SubscriptionListParams.builder().setCustomer(stripeCustomerId).build())
            .getData();
    return subscriptions.stream()
        .filter(s -> s.getStatus().equals("active"))
        .findFirst()
        .orElse(null);
  }

  enum CustomerMetadata {
    PROJECT_ID,
    CREATED_BY_EMAIL,
    CREATED_BY_USER_ID,
    CREATED_BY_NAME;

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }
}
