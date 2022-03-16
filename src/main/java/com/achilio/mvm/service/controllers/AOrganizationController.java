package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.resourcemanager.v3.Organization;
import com.google.cloud.resourcemanager.v3.OrganizationsClient;
import com.google.cloud.resourcemanager.v3.OrganizationsClient.SearchOrganizationsPagedResponse;
import com.google.cloud.resourcemanager.v3.OrganizationsSettings;
import com.google.cloud.resourcemanager.v3.SearchOrganizationsRequest;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class OrganizationController {

  @GetMapping(path = "/organization", produces = "application/json")
  @ApiOperation("List all organizations")
  public List<String> getAllOrganizations() throws IOException {
    SimpleGoogleCredentialsAuthentication authentication =
        (SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication();
    OrganizationsSettings organizationsSettings =
        OrganizationsSettings.newBuilder()
            .setCredentialsProvider(
                FixedCredentialsProvider.create(authentication.getCredentials()))
            .build();
    try (OrganizationsClient organizationsClient =
        OrganizationsClient.create(organizationsSettings)) {
      SearchOrganizationsRequest r = SearchOrganizationsRequest.newBuilder().build();
      SearchOrganizationsPagedResponse results = organizationsClient.searchOrganizations(r);
      return StreamSupport.stream(results.iterateAll().spliterator(), false)
          .map(Organization::getDirectoryCustomerId)
          .collect(Collectors.toList());
    }
  }
}
