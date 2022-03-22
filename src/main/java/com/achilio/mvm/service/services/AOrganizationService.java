package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.entities.FetchedOrganization;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.repositories.AOrganizationRepository;
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
  @Autowired private AOrganizationRepository organizationRepository;

  public List<AOrganization> getAllOrgOrUpdate() {
    List<FetchedOrganization> fetchedOrganizations = fetcherService.fetchAllOrganizations();
    return fetchedOrganizations.stream().map(this::findOrgOrUpdate).collect(Collectors.toList());
  }

  public AOrganization findOrgOrUpdate(FetchedOrganization fetchedOrganization) {
    return findAOrganization(fetchedOrganization.getName())
        .orElseGet(() -> createOrganization(fetchedOrganization));
  }

  public AOrganization createOrganization(FetchedOrganization fetchedOrganization) {
    // From 17th March 2022, the customerName is the organizationName (Not display name)
    Customer customer =
        stripeService.createCustomer(
            fetchedOrganization.getDisplayName(), fetchedOrganization.getName());
    AOrganization aOrganization =
        new AOrganization(
            fetchedOrganization.getName(),
            fetchedOrganization.getDisplayName(),
            customer.getId(),
            fetchedOrganization.getDirectoryCustomerId());
    return organizationRepository.save(aOrganization);
  }

  public Optional<AOrganization> findAOrganization(String organizationId) {
    return organizationRepository.findAOrganizationById(organizationId);
  }
}
