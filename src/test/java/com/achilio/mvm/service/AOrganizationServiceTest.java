package com.achilio.mvm.service;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.DefaultFetchedOrganization;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.AOrganization.OrganizationType;
import com.achilio.mvm.service.exceptions.OrganizationNotFoundException;
import com.achilio.mvm.service.repositories.AOrganizationRepository;
import com.achilio.mvm.service.services.AOrganizationService;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.StripeService;
import com.stripe.model.Customer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AOrganizationServiceTest {

  private final String ID1 = "organization/123456";
  private final String ID2 = "organization/789012";
  private final String ID3 = "organization/098765";
  private final String NAME1 = "achilio.com";
  private final String NAME2 = "example.com";
  private final String NAME3 = "example.net";
  private final String STRIPE_CUSTOMER1 = "cus_12345";
  private final String STRIPE_CUSTOMER2 = "cus_67890";
  private final String STRIPE_CUSTOMER3 = "cus_09876";
  private final String WORKSPACE1 = "workspace1";
  private final String WORKSPACE2 = "workspace2";
  private final String WORKSPACE3 = "workspace3";
  private final OrganizationType ORG1 = OrganizationType.ORGANIZATION;
  private final OrganizationType ORG2 = OrganizationType.ORGANIZATION;
  private final OrganizationType ORG3 = OrganizationType.ORGANIZATION;
  private final DefaultFetchedOrganization fetchedOrganization1 =
      new DefaultFetchedOrganization(ID1, NAME1, WORKSPACE1);
  private final DefaultFetchedOrganization fetchedOrganization2 =
      new DefaultFetchedOrganization(ID2, NAME2, WORKSPACE2);
  private final DefaultFetchedOrganization fetchedOrganization3 =
      new DefaultFetchedOrganization(ID3, NAME3, WORKSPACE3);
  @InjectMocks AOrganizationService service;
  @Mock AOrganizationRepository organizationRepository;
  @Mock FetcherService fetcherService;
  @Mock StripeService stripeService;
  @Mock Customer mockedCustomer3;
  private AOrganization organization1 =
      new AOrganization(ID1, NAME1, STRIPE_CUSTOMER1, ORG1, WORKSPACE1);
  private AOrganization organization2 =
      new AOrganization(ID2, NAME2, STRIPE_CUSTOMER2, ORG2, WORKSPACE2);
  private AOrganization organization3 =
      new AOrganization(ID3, NAME3, STRIPE_CUSTOMER3, ORG3, WORKSPACE3);

  @Before
  public void setup() {
    when(organizationRepository.findAOrganizationById(any()))
        .thenReturn(java.util.Optional.empty());
    when(organizationRepository.findAOrganizationById(ID1))
        .thenReturn(java.util.Optional.ofNullable(organization1));
    when(organizationRepository.findAOrganizationById(ID2))
        .thenReturn(java.util.Optional.ofNullable(organization2));
    when(organizationRepository.save(any())).then(returnsFirstArg());
    when(fetcherService.fetchAllOrganizations())
        .thenReturn(Arrays.asList(fetchedOrganization1, fetchedOrganization2));
    when(stripeService.createCustomer(organization3.getName(), organization3.getId()))
        .thenReturn(mockedCustomer3);
    when(mockedCustomer3.getId()).thenReturn(STRIPE_CUSTOMER3);
  }

  @Test
  public void findAOrganizationTest() {
    Optional<AOrganization> org1 = service.findAOrganization(ID1);
    Assert.assertTrue(org1.isPresent());
    assertOrganizationEquals(organization1, org1.get());

    Optional<AOrganization> org2 = service.findAOrganization(ID2);
    Assert.assertTrue(org2.isPresent());
    assertOrganizationEquals(organization2, org2.get());
  }

  @Test
  public void getAllOrgOrUpdateExistTest() {
    List<AOrganization> organizationList = service.getAllOrgOrCreate();
    Assert.assertEquals(2, organizationList.size());
    assertOrganizationEquals(organization1, organizationList.get(0));
    assertOrganizationEquals(organization2, organizationList.get(1));
  }

  @Test
  public void getAllOrgOrUpdateNotExistTest() {
    when(fetcherService.fetchAllOrganizations())
        .thenReturn(
            Arrays.asList(fetchedOrganization1, fetchedOrganization2, fetchedOrganization3));
    List<AOrganization> organizationList = service.getAllOrgOrCreate();
    Assert.assertEquals(3, organizationList.size());
    assertOrganizationEquals(organization1, organizationList.get(0));
    assertOrganizationEquals(organization2, organizationList.get(1));
    assertOrganizationEquals(organization3, organizationList.get(2));
  }

  @Test
  public void getOrganizationTest() {
    AOrganization organizationExists = service.getOrganization(ID1);
    assertOrganizationEquals(organization1, organizationExists);

    Assert.assertThrows(
        OrganizationNotFoundException.class, () -> service.getOrganization("idnotexists"));
  }

  @Test
  public void getAllOrgTest() {
    List<AOrganization> organizationList = service.getAllOrg();
    Assert.assertEquals(2, organizationList.size());
    assertOrganizationEquals(organization1, organizationList.get(0));
    assertOrganizationEquals(organization2, organizationList.get(1));

    when(fetcherService.fetchAllOrganizations()).thenReturn(Collections.emptyList());
    organizationList = service.getAllOrg();
    Assert.assertEquals(0, organizationList.size());
  }

  private void assertOrganizationEquals(AOrganization expected, AOrganization got) {
    Assert.assertEquals(expected.getId(), got.getId());
    Assert.assertEquals(expected.getName(), got.getName());
    Assert.assertEquals(expected.getStripeCustomerId(), got.getStripeCustomerId());
    Assert.assertEquals(expected.getGoogleWorkspaceId(), got.getGoogleWorkspaceId());
  }
}
