package com.achilio.mvm.service.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  /**
   * Create a Stripe customer
   *
   * @return the stripe customer ID
   */
  public String createCustomer(
      String projectId,
      String projectName,
      String createdByMail,
      String createdByUserId,
      String createdByName)
      throws StripeException {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(CustomerMetadata.PROJECT_ID.toString(), projectId);
    metadata.put(CustomerMetadata.PROJECT_NAME.toString(), projectName);
    metadata.put(CustomerMetadata.CREATED_BY_EMAIL.toString(), createdByMail);
    metadata.put(CustomerMetadata.CREATED_BY_USER_ID.toString(), createdByUserId);
    metadata.put(CustomerMetadata.CREATED_BY_NAME.toString(), createdByName);
    String formattedName = String.format("%s (%s)", projectName, projectId);
    CustomerCreateParams params =
        CustomerCreateParams.builder().setMetadata(metadata).setName(formattedName).build();
    return Customer.create(params).getId();
  }

  enum CustomerMetadata {
    PROJECT_ID,
    PROJECT_NAME,
    CREATED_BY_EMAIL,
    CREATED_BY_USER_ID,
    CREATED_BY_NAME;

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }
}
