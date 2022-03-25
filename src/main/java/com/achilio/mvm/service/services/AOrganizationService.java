package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.entities.FetchedOrganization;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.AOrganization.OrganizationType;
import com.achilio.mvm.service.exceptions.OrganizationNotFoundException;
import com.achilio.mvm.service.repositories.AOrganizationRepository;
import com.google.api.services.oauth2.model.Userinfo;
import com.stripe.model.Customer;
import java.util.Collections;
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
  @Autowired private ProjectService projectService;
  @Autowired private AOrganizationRepository organizationRepository;

  public List<AOrganization> getAllOrg() {
    return fetcherService.fetchAllOrganizations().stream()
        .filter(o -> organizationExists(o.getName()))
        .map(o -> getOrganization(o.getName()))
        .collect(Collectors.toList());
  }

  public List<AOrganization> getAllOrgOrCreate() {
    List<FetchedOrganization> fetchedOrganizations = fetcherService.fetchAllOrganizations();
    if (fetchedOrganizations.isEmpty()) {
      Userinfo user = fetcherService.getUserInfo();
      return Collections.singletonList(findOrganizationNoOrgOrCreate(user));
    }
    return fetchedOrganizations.stream().map(this::findOrgOrCreate).collect(Collectors.toList());
  }

  public AOrganization findOrgOrCreate(FetchedOrganization fetchedOrganization) {
    return findAOrganization(fetchedOrganization.getName())
        .orElseGet(() -> createOrganization(fetchedOrganization));
  }

  public void createProjectStructure(AOrganization organization) {
    List<FetchedProject> projectList = fetcherService.fetchAllProjectsFromOrg(organization);
    projectList.forEach(projectService::createProjectFromFetchedProject);
  }

  public AOrganization findOrganizationNoOrgOrCreate(Userinfo user) {
    return findAOrganization(user.getEmail()).orElseGet(() -> createOrganizationNoOrg(user));
  }

  public AOrganization createOrganizationNoOrg(Userinfo user) {
    // In case of a No Org customer, the Customer Name is the full name, the id and org_id are both
    // the user Email
    Customer customer = stripeService.createCustomer(user.getName(), user.getEmail());
    AOrganization organization =
        new AOrganization(
            user.getId(), user.getEmail(), customer.getId(), OrganizationType.NO_ORGANIZATION);
    return organizationRepository.save(organization);
  }

  public AOrganization createOrganization(FetchedOrganization fetchedOrganization) {
    // the customerName is the FetchedOrganization.getName() (Not display name)
    Customer customer =
        stripeService.createCustomer(
            fetchedOrganization.getDisplayName(), fetchedOrganization.getName());
    AOrganization organization =
        new AOrganization(
            fetchedOrganization.getName(),
            fetchedOrganization.getDisplayName(),
            customer.getId(),
            OrganizationType.ORGANIZATION,
            fetchedOrganization.getDirectoryCustomerId());
    return organizationRepository.save(organization);
  }

  public Optional<AOrganization> findAOrganization(String organizationId) {
    return organizationRepository.findAOrganizationById(organizationId);
  }

  private boolean organizationExists(String organizationId) {
    return organizationRepository.findAOrganizationById(organizationId).isPresent();
  }

  public AOrganization getOrganization(String organizationId) {
    return findAOrganization(organizationId)
        .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
  }
}
