package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.UserContextHelper.getStripeSubscriptionId;
import static com.achilio.mvm.service.UserContextHelper.getUserProfile;
import static com.achilio.mvm.service.services.StripeService.StripeMetadata.PROJECT_ID;
import static java.util.stream.Collectors.toList;

import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.models.PlanPrice;
import com.achilio.mvm.service.models.ProjectPlan;
import com.achilio.mvm.service.models.ProjectPlan.PossibleAction;
import com.achilio.mvm.service.models.ProjectSubscription;
import com.achilio.mvm.service.models.UserProfile;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams.Status;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.SubscriptionUpdateParams.ProrationBehavior;
import io.micrometer.core.instrument.util.StringUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
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

  @Autowired private ProjectService projectService;
  @Autowired private GooglePublisherService googlePublisherService;

  public String createPortalSession() throws StripeException {
    Stripe.apiKey = API_KEY;
    com.stripe.param.billingportal.SessionCreateParams params =
        com.stripe.param.billingportal.SessionCreateParams.builder()
            .setCustomer("cus_LPbXuqfN7jlewJ")
            .setReturnUrl("http://localhost:8081/home/projects")
            .build();

    com.stripe.model.billingportal.Session session =
        com.stripe.model.billingportal.Session.create(params);

    return session.getUrl();
  }

  public SubscriptionSchedule getSubscription() throws StripeException {
    Stripe.apiKey = API_KEY;
    if (StringUtils.isEmpty(getStripeSubscriptionId())) {
      return null;
    }
    return SubscriptionSchedule.retrieve(getStripeSubscriptionId());
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

  /**
   * Create a Stripe customer
   *
   * @return the stripe customer ID
   */
  public Customer createCustomer(String customerName, String organizationId) {
    UserProfile userProfile = getUserProfile();
    String userEmail = userProfile.getEmail();
    String userName = userProfile.getName();
    return createCustomer(userEmail, userName, customerName, organizationId);
  }

  private Customer createCustomer(
      String userEmail, String userName, String customerName, String organizationId) {
    Stripe.apiKey = API_KEY;
    try {
      Map<String, String> metadata = new HashMap<>();
      metadata.put(StripeMetadata.ORGANIZATION_ID.getValue(), organizationId);
      metadata.put(StripeMetadata.CREATED_BY_EMAIL.getValue(), userEmail);
      metadata.put(StripeMetadata.CREATED_BY_NAME.getValue(), userName);
      CustomerCreateParams params =
          CustomerCreateParams.builder()
              .setName(customerName)
              .setMetadata(metadata)
              .setEmail(userEmail)
              .build();
      Customer customer = Customer.create(params);
      LOGGER.debug("Creating new customer {} ({})", customer.getId(), customerName);
      return customer;
    } catch (StripeException e) {
      LOGGER.error("Error while creating the stripe customer {}", customerName, e);
    }
    return null;
  }

  public Customer getCustomer(String customerId) throws StripeException {
    Stripe.apiKey = API_KEY;
    return Customer.retrieve(customerId);
  }

  public ProjectSubscription createSubscription(Customer customer, String priceId, String projectId)
      throws StripeException {
    Project project = projectService.getProjectAsUser(projectId);
    Stripe.apiKey = API_KEY;
    Map<String, String> metadata = new HashMap<>();
    metadata.put(PROJECT_ID.getValue(), projectId);
    SubscriptionCreateParams subCreateParams =
        SubscriptionCreateParams.builder()
            .setCustomer(customer.getId())
            .setMetadata(metadata)
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
            .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
            .build();
    Subscription s = Subscription.create(subCreateParams);
    projectService.updateProjectSubscription(project, s.getId());
    LOGGER.info("Create a new subscription {} for customer {}", s.getId(), customer.getId());
    return toProjectSubscription(s);
  }

  public ProjectSubscription getSubscription(String subscriptionId) {
    return findSubscription(subscriptionId)
        .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
  }

  public Optional<ProjectSubscription> findSubscription(String subscriptionId) {
    if (subscriptionId == null) {
      return Optional.empty();
    }
    Stripe.apiKey = API_KEY;
    try {
      return Optional.of(toProjectSubscription(Subscription.retrieve(subscriptionId)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<ProjectPlan> getPlans(String projectId) throws StripeException {
    Stripe.apiKey = API_KEY;
    Project project = projectService.getProjectAsUser(projectId);
    ProductListParams p = ProductListParams.builder().setActive(true).build();
    // Retrieve product as project plans
    List<ProjectPlan> plans =
        Product.list(p).getData().parallelStream().map(this::toProjectPlan).collect(toList());
    // Find subscription
    Optional<ProjectSubscription> activeSubscription =
        findSubscription(project.getStripeSubscriptionId());
    if (activeSubscription.isPresent()) {
      // Find for the associated plan
      Optional<ProjectPlan> activePlan =
          plans.stream()
              .filter(plan -> plan.hasPriceId(activeSubscription.get().getPriceId()))
              .findFirst();
      activePlan.ifPresent(projectPlan -> projectPlan.setSubscription(activeSubscription.get()));
    }
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

  public void handleSubscription(Subscription subscription)
      throws StripeException, IOException, ExecutionException, InterruptedException {
    Stripe.apiKey = API_KEY;
    Project project = getProjectIdFromSubscription(subscription);
    Product product =
        Product.retrieve(subscription.getItems().getData().get(0).getPrice().getProduct());
    if (product == null) {
      String exMsg = String.format("Product not found for subscription %s", subscription.getId());
      throw new IllegalArgumentException(exMsg);
    }
    if (subscription.getStatus().equals(Status.ACTIVE.getValue())) {
      projectService.activateProject(project);
      googlePublisherService.publishProjectActivation(project.getProjectId());
      projectService.updatePlanSettings(project, product);
    } else {
      projectService.deactivateProject(project);
    }
  }

  private Project getProjectIdFromSubscription(Subscription subscription) {
    Map<String, String> metadata = subscription.getMetadata();
    if (metadata.containsKey(PROJECT_ID.getValue())) {
      String projectId = metadata.get(PROJECT_ID.getValue());
      return projectService.getProject(projectId);
    }
    throw new IllegalArgumentException("Subscription doesn't contains the project id metadata");
  }

  // Private methods

  private List<PlanPrice> getPrices(String productId) throws StripeException {
    Stripe.apiKey = API_KEY;
    return Price.list(PriceListParams.builder().setProduct(productId).setActive(true).build())
        .getData()
        .stream()
        .map(this::toProjectPlanPrice)
        .collect(toList());
  }

  private ProjectPlan toProjectPlan(Product product) {
    String imageUrl = product.getImages().isEmpty() ? null : product.getImages().get(0);
    String description = product.getDescription();
    String name = product.getName();
    String id = product.getId();
    ProjectPlan projectPlan = new ProjectPlan(name, id, imageUrl, description);
    try {
      List<PlanPrice> prices = getPrices(id);
      projectPlan.setPricing(prices);
    } catch (StripeException e) {
      LOGGER.error("Error while fetching pricing for product {}", id, e);
    }
    return projectPlan;
  }

  private PlanPrice toProjectPlanPrice(Price price) {
    String interval = price.getRecurring() == null ? null : price.getRecurring().getInterval();
    return new PlanPrice(price.getId(), price.getUnitAmount(), price.getCurrency(), interval);
  }

  private ProjectSubscription toProjectSubscription(Subscription s) {
    List<SubscriptionItem> items = s.getItems().getData();
    if (!items.isEmpty()) {
      return new ProjectSubscription(s.getId(), s.getStatus(), items.get(0).getPrice().getId());
    }
    throw new IllegalArgumentException("No subscription item found in subscription " + s.getId());
  }

  enum StripeMetadata {
    PROJECT_ID,
    ORGANIZATION_ID,
    CREATED_BY_EMAIL,
    CREATED_BY_NAME;

    public String getValue() {
      return this.name().toLowerCase();
    }
  }
}
