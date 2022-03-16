package com.achilio.mvm.service.services;

import org.springframework.stereotype.Service;

/** Services to manage organization resources. */
@Service
public class OrganizationService {

  //  public Organization findOrgOrCreate(
  //      com.google.cloud.resourcemanager.v3.Organization fetchedOrganization) {
  //    return findProject(projectId).orElseGet(() -> createOrganization(projectId));
  //  }
  //
  //  public Project createOrganization(String projectId) {
  //    // To date the customerName is the projectId
  //    Customer customer = stripeService.createCustomer(projectId, projectId);
  //    Project project = new Project(projectId, customer.getId());
  //    return projectRepository.save(project);
  //  }
  //
  //  public Optional<Project> findProject(String projectId) {
  //    return projectRepository.findByProjectId(projectId);
  //  }
}
