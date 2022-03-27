package com.achilio.mvm.service.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.services.AOrganizationService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class AConnectionController {

  @Autowired AOrganizationService organizationService;

  @GetMapping(path = "/organization", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all organizations")
  public List<AOrganization> getAllOrganizations() {
    return organizationService.getAllOrg();
  }
}