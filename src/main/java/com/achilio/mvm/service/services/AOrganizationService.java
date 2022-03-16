package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.repositories.AOrganizationRepository;
import com.google.cloud.resourcemanager.v3.Organization;
import com.stripe.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage organization resources. */
@Service
public class AOrganizationService {

  @Autowired private FetcherService fetcherService;
  @Autowired private StripeService stripeService;
  @Autowired private AOrganizationRepository aOrganizationRepository;

  public List<AOrganization> getAllOrgOrUpdate() {
    List<Organization> fetchedOrganizations = fetcherService.fetchAllOrganizations();
    return fetchedOrganizations.stream().map(this::findOrgOrUpdate).collect(Collectors.toList());
  }

  public AOrganization findOrgOrUpdate(Organization fetchedOrganization) {
    return findAOrganization(fetchedOrganization.getName())
        .orElseGet(() -> createOrganization(fetchedOrganization));
  }

  public AOrganization createOrganization(Organization organization) {
    // From 17th March 2022, the customerName is the organizationName (Not display name)
    Customer customer =
        stripeService.createCustomer(organization.getDisplayName(), organization.getName());
    AOrganization aOrganization =
        new AOrganization(
            organization.getName(),
            organization.getDisplayName(),
            customer.getId(),
            organization.getDirectoryCustomerId());
    return aOrganizationRepository.save(aOrganization);
  }

  public Optional<AOrganization> findAOrganization(String organizationId) {
    return aOrganizationRepository.findAOrganizationById(organizationId);
  }
}
