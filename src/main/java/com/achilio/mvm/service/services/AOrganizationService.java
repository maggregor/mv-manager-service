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

  public List<AOrganization> getAllOrg() {
    return organizationRepository.findAll();
  }

  public List<AOrganization> getAllOrgOrCreate() {
    List<FetchedOrganization> fetchedOrganizations = fetcherService.fetchAllOrganizations();
    return fetchedOrganizations.stream().map(this::findOrgOrCreate).collect(Collectors.toList());
  }

  public AOrganization findOrgOrCreate(FetchedOrganization fetchedOrganization) {
    return findAOrganization(fetchedOrganization.getName())
        .orElseGet(() -> createOrganization(fetchedOrganization));
  }

  public AOrganization createOrganization(FetchedOrganization fetchedOrganization) {
    // the customerName is the FetchedOrganization.getName() (Not display
    // name)
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
