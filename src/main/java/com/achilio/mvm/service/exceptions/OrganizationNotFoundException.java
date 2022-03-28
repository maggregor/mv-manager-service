package com.achilio.mvm.service.exceptions;

public class OrganizationNotFoundException extends IllegalArgumentException {

  public OrganizationNotFoundException(String organizationId) {
    super(String.format("Organization %s not found", organizationId));
  }
}
