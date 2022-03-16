package com.achilio.mvm.service.controllers;

import com.google.cloud.resourcemanager.v3.Organization;
import com.google.cloud.resourcemanager.v3.OrganizationsClient;
import com.google.cloud.resourcemanager.v3.OrganizationsClient.SearchOrganizationsPagedResponse;
import com.google.cloud.resourcemanager.v3.SearchOrganizationsRequest;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
  public List<Organization> getAllOrganizations() throws IOException {
    try (OrganizationsClient organizationsClient = OrganizationsClient.create()) {
      SearchOrganizationsRequest r = SearchOrganizationsRequest.newBuilder().build();
      SearchOrganizationsPagedResponse results = organizationsClient.searchOrganizations(r);
      return StreamSupport.stream(results.iterateAll().spliterator(), false)
          .collect(Collectors.toList());
    }
  }
}
