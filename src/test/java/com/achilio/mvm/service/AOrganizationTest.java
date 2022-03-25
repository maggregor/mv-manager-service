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
  private final AOrganization organization1 =
      new AOrganization(orgId, orgName, stripeId, organizationType, googleWorkspaceId);
  private final AOrganization organization2 =
      new AOrganization(orgId, orgName, stripeId, organizationType, googleWorkspaceId);
  private final AOrganization organization3 =
      new AOrganization(orgId, orgName, stripeId, organizationType, "anotherWorkspaceId");
  private final AOrganization organization4 =
      new AOrganization(
          orgId, orgName, stripeId, OrganizationType.NO_ORGANIZATION, googleWorkspaceId);
  private final AOrganization organization5 =
      new AOrganization(orgId, orgName, "anotherCustomerId", organizationType, googleWorkspaceId);
  private final AOrganization organization6 =
      new AOrganization(orgId, "anotherOrgName", stripeId, organizationType, googleWorkspaceId);
  private final AOrganization organization7 =
      new AOrganization(
          "organization/anotherId", orgName, stripeId, organizationType, googleWorkspaceId);

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

  @Test
  public void testEquals() {
    Assert.assertEquals(organization1, organization1);
    Assert.assertEquals(organization1, organization2);
    Assert.assertNotEquals(organization1, organization3);
    Assert.assertNotEquals(organization1, organization4);
    Assert.assertNotEquals(organization1, organization5);
    Assert.assertNotEquals(organization1, organization6);
    Assert.assertNotEquals(organization1, organization7);
    Assert.assertNotEquals(null, organization1);
    Assert.assertNotEquals(organization1, orgId);
  }

  @Test
  public void testHashcode() {
    Assert.assertEquals(organization1.hashCode(), organization2.hashCode());
    Assert.assertNotEquals(organization1.hashCode(), organization3.hashCode());
    Assert.assertNotEquals(organization1.hashCode(), organization4.hashCode());
    Assert.assertNotEquals(organization1.hashCode(), organization5.hashCode());
    Assert.assertNotEquals(organization1.hashCode(), organization6.hashCode());
    Assert.assertNotEquals(organization1.hashCode(), organization7.hashCode());
  }
}
