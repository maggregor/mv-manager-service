package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.AOrganization.OrganizationType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AOrganizationTest {

  private final String orgId = "organization/1234567";
  private final String orgName = "example.com";
  private final String stripeId = "stripeCustomerId";
  private final OrganizationType organizationType = OrganizationType.ORGANIZATION;
  private final String googleWorkspaceId = "googleWorkspaceId";

  @Test
  public void simpleValidation() {
    AOrganization organization =
        new AOrganization(orgId, orgName, stripeId, organizationType, googleWorkspaceId);
    Assert.assertEquals(orgId, organization.getId());
    Assert.assertEquals(orgName, organization.getName());
    Assert.assertEquals(stripeId, organization.getStripeCustomerId());
    Assert.assertEquals(organizationType, organization.getOrganizationType());
    Assert.assertEquals(googleWorkspaceId, organization.getGoogleWorkspaceId());
  }
}
