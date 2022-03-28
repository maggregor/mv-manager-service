package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.AOrganizationController;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.AOrganization.OrganizationType;
import com.achilio.mvm.service.services.AOrganizationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class AOrganizationControllerTest {

  private static final String ORGANIZATION_ID1 = "organization/12345";
  private static final String ORGANIZATION_ID2 = "organization/67890";
  private static final String ORGANIZATION_NAME1 = "achilio.com";
  private static final String ORGANIZATION_NAME2 = "example.com";
  private static final String ORGANIZATION_STRIPE_CUSTOMER1 = "cus_12345";
  private static final String ORGANIZATION_STRIPE_CUSTOMER2 = "cus_67890";
  private static final String WORKSPACE_ID1 = "workspaceId1";
  private static final String WORKSPACE_ID2 = "workspaceId2";
  private static AOrganization organization1 =
      new AOrganization(
          ORGANIZATION_ID1,
          ORGANIZATION_NAME1,
          ORGANIZATION_STRIPE_CUSTOMER1,
          OrganizationType.ORGANIZATION,
          WORKSPACE_ID1);
  private static AOrganization organization2 =
      new AOrganization(
          ORGANIZATION_ID2,
          ORGANIZATION_NAME2,
          ORGANIZATION_STRIPE_CUSTOMER2,
          OrganizationType.ORGANIZATION,
          WORKSPACE_ID2);
  private static List<AOrganization> allOrganizations = Arrays.asList(organization1, organization2);
  private final ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks AOrganizationController controller;
  @Mock AOrganizationService mockOrganizationService;

  @Before
  public void setup() {
    when(mockOrganizationService.getAllOrg()).thenReturn(allOrganizations);
    when(mockOrganizationService.getAllOrgOrCreate()).thenReturn(allOrganizations);
  }

  @Test
  public void getAllOrganizations() throws JsonProcessingException {
    List<AOrganization> organizationList = controller.getAllOrganizations();
    assertOrganizationListEquals(allOrganizations, organizationList);
  }

  @Test
  public void getAllOrganizationsNone() throws JsonProcessingException {
    when(mockOrganizationService.getAllOrg()).thenReturn(Collections.emptyList());
    List<AOrganization> organizationList = controller.getAllOrganizations();
    assertOrganizationListEquals(Collections.emptyList(), organizationList);
  }

  @Test
  public void createProjectStructure() {
    controller.createProjectStructure();
    Mockito.verify(mockOrganizationService, Mockito.timeout(1000).times(2))
        .createProjectStructure(ArgumentMatchers.any(AOrganization.class));
  }

  private void assertOrganizationResponseEquals(
      AOrganization expected, AOrganization actualProjectResponse) throws JsonProcessingException {
    String actualJson = objectMapper.writeValueAsString(actualProjectResponse);
    JsonNode jsonNode = objectMapper.readTree(actualJson);
    assertEquals(expected.getId(), jsonNode.get("id").asText());
    assertEquals(expected.getName(), jsonNode.get("name").asText());
    assertEquals(expected.getStripeCustomerId(), jsonNode.get("stripeCustomerId").asText());
    assertEquals(expected.getGoogleWorkspaceId(), jsonNode.get("googleWorkspaceId").asText());
    assertEquals(
        expected.getOrganizationType().toString(), jsonNode.get("organizationType").asText());
  }

  private void assertOrganizationListEquals(
      List<AOrganization> expected, List<AOrganization> actualAOrganizationList)
      throws JsonProcessingException {
    Assert.assertEquals(expected.size(), actualAOrganizationList.size());
    String actualJson = objectMapper.writeValueAsString(actualAOrganizationList);
    JsonNode jsonNode = objectMapper.readTree(actualJson);
    Assert.assertTrue(jsonNode instanceof ArrayNode);
    for (int i = 0; i < expected.size(); i++) {
      assertOrganizationResponseEquals(expected.get(i), actualAOrganizationList.get(i));
    }
  }
}
