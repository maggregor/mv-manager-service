package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.services.AOrganizationService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class AOrganizationController {

  @Autowired AOrganizationService aOrganizationService;

  @GetMapping(path = "/organization", produces = "application/json")
  @ApiOperation("List all organizations")
  public List<AOrganization> getAllOrganizations() throws IOException {
    return aOrganizationService.getAllOrgOrCreate();
  }
}
