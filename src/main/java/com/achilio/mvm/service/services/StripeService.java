package com.achilio.mvm.service.services;

import static java.util.stream.Collectors.toList;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.billingportal.Session;
import com.stripe.param.CustomerListPaymentMethodsParams;
import com.stripe.param.CustomerListPaymentMethodsParams.Type;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.SubscriptionItemUpdateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.billingportal.SessionCreateParams;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StripeService.class);

  @Value("${stripe.api.key}")
  private String API_KEY;

  @Value("${server.webapp.endpoint}")
  private String WEBAPP_ENDPOINT;

  public StripeService() {}

  public String createPortalSession(String customerId) throws StripeException {
    final String returnToWebAppUrl = WEBAPP_ENDPOINT + "/home/billing";
    Stripe.apiKey = API_KEY;
    SessionCreateParams params =
        SessionCreateParams.builder()
            .setCustomer(customerId)
            .setReturnUrl(returnToWebAppUrl)
            .build();
    Session session = Session.create(params);
    return session.getUrl();
  }

  public void updateSubscriptionQuantity(String customerId, Long quantity) {
    try {
      Subscription subscription = getSubscription(customerId);
      SubscriptionItem subscriptionItem = subscription.getItems().getData().get(0);
      SubscriptionItemUpdateParams params =
          SubscriptionItemUpdateParams.builder().setQuantity(quantity).build();
      subscriptionItem.update(params);
    } catch (StripeException e) {
      LOGGER.error(
          "Error while updating subscription of customer {} with quantity {}",
          customerId,
          quantity,
          e);
    }
  }

  public Subscription getSubscription(String customerId) throws StripeException {
    Stripe.apiKey = API_KEY;
    SubscriptionListParams params =
        SubscriptionListParams.builder().setCustomer(customerId).build();
    List<Subscription> subscriptions = Subscription.list(params).getData();
    if (subscriptions.size() > 1) {
      LOGGER.error("The customerId {} has more than one Stripe subscription", customerId);
      throw new IllegalArgumentException("More than one subscription");
    } else if (subscriptions.isEmpty()) {
      LOGGER.error("The customerId {} has not Stripe subscription", customerId);
      throw new IllegalArgumentException("No Stripe subscription");
    }
    return subscriptions.get(0);
  }

  public Customer getCustomer(String customerId) throws StripeException {
    return Customer.retrieve(customerId);
  }

  public List<Product> getAllProducts() throws StripeException {
    Stripe.apiKey = API_KEY;
    return Product.list(ProductListParams.builder().build()).getData().stream()
        .filter(Product::getActive)
        .filter(this::isAvailable)
        .collect(toList());
  }

  public List<Price> getAllPrices() throws StripeException {
    Stripe.apiKey = API_KEY;
    return Price.list(PriceListParams.builder().setActive(true).build()).getData().stream()
        .filter(this::isAvailable)
        .collect(toList());
  }

  public boolean isAvailable(Price price) {
    return price.getMetadata().entrySet().stream()
        .anyMatch(e -> e.getKey().equals("available") && e.getValue().equalsIgnoreCase("true"));
  }

  public boolean isAvailable(Product product) {
    return product.getMetadata().entrySet().stream()
        .anyMatch(e -> e.getKey().equals("available") && e.getValue().equalsIgnoreCase("true"));
  }

  public List<SubscriptionCheck> subscriptionChecks(String customerId) throws StripeException {
    List<SubscriptionCheck> checks = new ArrayList<>();
    Customer customer = Customer.retrieve(customerId);
    CustomerListPaymentMethodsParams params =
        CustomerListPaymentMethodsParams.builder().setType(Type.CARD).build();
    PaymentMethodCollection paymentMethods = customer.listPaymentMethods(params);
    if (paymentMethods.getData().isEmpty()) {
      checks.add(SubscriptionCheck.NO_PAYMENT_METHOD);
    }
    return checks;
  }

  public enum SubscriptionCheck {
    NO_PAYMENT_METHOD,
  }
}
