package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.models.ProjectPlan;
import com.achilio.mvm.service.models.ProjectPlan.PossibleAction;
import com.achilio.mvm.service.models.ProjectPlanPrice;
import com.achilio.mvm.service.models.ProjectSubscription;
import com.google.api.services.oauth2.model.Userinfo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionListParams.Status;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.SubscriptionUpdateParams.ProrationBehavior;
import io.micrometer.core.instrument.util.StringUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StripeService.class);

  @Value("${stripe.api.key}")
  private String API_KEY;

  @Autowired private FetcherService fetcherService;
  @Autowired private ProjectService projectService;
  @Autowired private GooglePublisherService googlePublisherService;

  /**
   * Create a Stripe customer
   *
   * @return the stripe customer ID
   */
  public Customer createCustomer(String projectId) {
    Userinfo userInfo = fetcherService.getUserInfo();
    String email = userInfo.getEmail();
    String name = userInfo.getName();
    return createCustomer(email, name, projectId);
  }

  public Customer createCustomer(String customerMail, String customerName, String projectId) {
    Stripe.apiKey = API_KEY;
    try {
      Map<String, String> metadata = new HashMap<>();
      metadata.put(CustomerMetadata.PROJECT_ID.getValue(), projectId);
      metadata.put(CustomerMetadata.CREATED_BY_EMAIL.getValue(), customerMail);
      metadata.put(CustomerMetadata.CREATED_BY_NAME.getValue(), customerName);
      CustomerCreateParams params =
          CustomerCreateParams.builder()
              .setMetadata(metadata)
              .setEmail(customerMail)
              .setName(projectId)
              .build();
      Customer customer = Customer.create(params);
      LOGGER.debug("Creating new customer {} for project {}", customer.getId(), projectId);
      return customer;
    } catch (StripeException e) {
      LOGGER.error("Error while creating the stripe customer for project {}", projectId, e);
    }
    return null;
  }

  /**
   * Get customer by project id
   *
   * <p>Check in the customer project-id metadata
   *
   * @param projectId
   * @return
   * @throws StripeException
   */
  public Customer getCustomerByProjectId(String projectId) throws StripeException {
    Stripe.apiKey = API_KEY;
    return Customer.list(CustomerListParams.builder().build()).getData().stream()
        .filter(c -> isCustomerOfProjectId(c, projectId))
        .findFirst()
        .orElse(createCustomer(projectId));
  }

  private boolean isCustomerOfProjectId(Customer customer, String projectId) {
    if (customer.getMetadata().containsKey(CustomerMetadata.PROJECT_ID.getValue())) {
      return customer.getMetadata().get(CustomerMetadata.PROJECT_ID.getValue()).equals(projectId);
    }
    return false;
  }

  public ProjectSubscription createSubscription(Customer customer, String priceId)
      throws StripeException {
    Stripe.apiKey = API_KEY;
    SubscriptionCreateParams subCreateParams =
        SubscriptionCreateParams.builder()
            .setCustomer(customer.getId())
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
            .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
            .build();
    Subscription s = Subscription.create(subCreateParams);
    LOGGER.info("Create a new subscription {} for customer {}", s.getId(), customer.getId());
    return toProjectSubscription(s);
  }

  public ProjectSubscription getSubscription(String subscriptionId) throws StripeException {
    Stripe.apiKey = API_KEY;
    return toProjectSubscription(Subscription.retrieve(subscriptionId));
  }

  public ProjectSubscription getSubscriptionByPriceId(String customerId, String priceId)
      throws StripeException {
    Stripe.apiKey = API_KEY;
    Subscription subscription =
        Subscription.list(
                SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .setPrice(priceId)
                    .setStatus(Status.ACTIVE)
                    .build())
            .getData()
            .stream()
            .findFirst()
            .orElse(null);
    return subscription == null ? null : toProjectSubscription(subscription);
  }

  public List<ProjectPlan> getPlans(Customer customer) throws StripeException {
    Stripe.apiKey = API_KEY;
    ProductListParams p = ProductListParams.builder().setActive(true).build();
    List<ProjectPlan> plans =
        Product.list(p).getData().parallelStream()
            .map(plan -> toProjectPlan(plan, customer.getId()))
            .collect(Collectors.toList());
    applyPossibleActions(plans);
    return plans;
  }

  public void applyPossibleActions(List<ProjectPlan> plans) {
    ProjectPlan enabledPlan =
        plans.stream().filter(ProjectPlan::isEnabled).findFirst().orElse(null);
    plans.forEach(
        plan -> {
          if (enabledPlan == null) {
            plan.setPossibleAction(PossibleAction.SUBSCRIBE);
          } else if (plan.isEnabled()) {
            plan.setPossibleAction(PossibleAction.CANCEL);
          } else if (!enabledPlan.getPrices().isEmpty() && !plan.getPrices().isEmpty()) {
            boolean upgrade =
                enabledPlan.getPrices().get(0).getAmount() < plan.getPrices().get(0).getAmount();
            plan.setPossibleAction(upgrade ? PossibleAction.UPGRADE : PossibleAction.DOWNGRADE);
          } else {
            plan.setPossibleAction(PossibleAction.SUBSCRIBE);
          }
        });
  }

  public ProjectSubscription changeSubscriptionPricing(String subscriptionId, String newPriceId)
      throws StripeException {
    Stripe.apiKey = API_KEY;
    Subscription s = Subscription.retrieve(subscriptionId);
    SubscriptionItem item = s.getItems().getData().get(0);
    String actualPrice = item.getPrice().getId();
    SubscriptionUpdateParams params =
        SubscriptionUpdateParams.builder()
            .setCancelAtPeriodEnd(true)
            .setProrationBehavior(ProrationBehavior.ALWAYS_INVOICE)
            .setPaymentBehavior(SubscriptionUpdateParams.PaymentBehavior.PENDING_IF_INCOMPLETE)
            .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS)
            .addItem(
                SubscriptionUpdateParams.Item.builder()
                    .setId(item.getId())
                    .setPrice(newPriceId)
                    .build())
            .build();
    LOGGER.info("Subscription {} price update from {} to {}", s.getId(), actualPrice, newPriceId);
    return toProjectSubscription(s.update(params));
  }

  public ProjectSubscription cancelSubscription(String subscriptionId) throws StripeException {
    Stripe.apiKey = API_KEY;
    Subscription s = Subscription.retrieve(subscriptionId);
    LOGGER.info("Cancel subscription {}", subscriptionId);
    return toProjectSubscription(s.cancel());
  }

  public String getLatestIntentClientSecret(String subscriptionId) throws StripeException {
    Subscription subscription = Subscription.retrieve(subscriptionId);
    Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
    if (StringUtils.isEmpty(invoice.getPaymentIntent())) {
      return null;
    }
    return PaymentIntent.retrieve(invoice.getPaymentIntent()).getClientSecret();
  }

  public void handleSubscription(Subscription subscription, String customerId)
      throws StripeException, IOException, ExecutionException, InterruptedException {
    Stripe.apiKey = API_KEY;
    String projectId =
        Customer.retrieve(customerId).getMetadata().get(CustomerMetadata.PROJECT_ID.getValue());
    Project project = projectService.getProject(projectId);
    if (subscription.getStatus().equals(Status.ACTIVE.getValue())) {
      projectService.activateProject(project);
      googlePublisherService.publishProjectActivation(projectId);
    } else {
      projectService.deactivateProject(project);
    }
  }

  // Private methods

  private List<ProjectPlanPrice> getPrices(String productId) throws StripeException {
    Stripe.apiKey = API_KEY;
    return Price.list(PriceListParams.builder().setProduct(productId).setActive(true).build())
        .getData()
        .stream()
        .map(this::toProjectPlanPrice)
        .collect(Collectors.toList());
  }

  private ProjectPlan toProjectPlan(Product product, String customerId) {
    String imageUrl = product.getImages().isEmpty() ? null : product.getImages().get(0);
    String description = product.getDescription();
    String name = product.getName();
    String id = product.getId();
    ProjectPlan projectPlan = new ProjectPlan(name, id, imageUrl, description);
    try {
      List<ProjectPlanPrice> prices = getPrices(id);
      projectPlan.setPricing(prices);
      ProjectSubscription subscription = null;
      for (ProjectPlanPrice price : prices) {
        ProjectSubscription s = getSubscriptionByPriceId(customerId, price.getStripePriceId());
        if (s != null) {
          subscription = s;
        }
      }
      if (subscription != null) {
        projectPlan.setSubscription(subscription);
      }
    } catch (StripeException e) {
      LOGGER.error("Error while fetching product {} ({}) ", name, id, e);
    }
    return projectPlan;
  }

  private ProjectPlanPrice toProjectPlanPrice(Price price) {
    String interval = price.getRecurring() == null ? null : price.getRecurring().getInterval();
    return new ProjectPlanPrice(
        price.getId(), price.getUnitAmount(), price.getCurrency(), interval);
  }

  private ProjectSubscription toProjectSubscription(Subscription s) {
    return new ProjectSubscription(s.getId(), s.getStatus());
  }

  enum CustomerMetadata {
    PROJECT_ID,
    CREATED_BY_EMAIL,
    CREATED_BY_NAME;

    public String getValue() {
      return this.name().toLowerCase();
    }
  }
}