package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ConnectionController;
import com.achilio.mvm.service.controllers.ConnectionNameResponse;
import com.achilio.mvm.service.controllers.requests.ConnectionResponse;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionResponse;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.services.ConnectionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class ConnectionControllerTest {

  @InjectMocks ConnectionController mockedController;
  @Mock ConnectionService mockedService;
  @Mock private UserProfile mockedUserProfile;

  @Before
  public void setup() {
    // Mock security context for the getContextTeamName() method in Controller
    Authentication mockedAuth = mock(Authentication.class);
    SecurityContext mockedSecurityContext = mock(SecurityContext.class);
    when(mockedUserProfile.getTeamName()).thenReturn("myTeam");
    when(mockedAuth.getDetails()).thenReturn(mockedUserProfile);
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
    //
    ServiceAccountConnection mockedConnection = mock(ServiceAccountConnection.class);
    ServiceAccountConnection mockedConnection2 = mock(ServiceAccountConnection.class);
    when(mockedConnection.getId()).thenReturn(1L);
    when(mockedConnection.getName()).thenReturn("My Connection");
    when(mockedConnection.getContent()).thenReturn("SA_content_1");
    when(mockedConnection2.getContent()).thenReturn("SA_content_2");
    when(mockedConnection2.getId()).thenReturn(2L);
    when(mockedConnection2.getName()).thenReturn("My Connection 2");
    when(mockedService.getConnection(1L, "myTeam")).thenReturn(mockedConnection);
    when(mockedService.getConnection(2L, "myTeam")).thenReturn(mockedConnection2);
    when(mockedService.getAllConnections(mockedUserProfile.getTeamName()))
        .thenReturn(Arrays.asList(mockedConnection, mockedConnection2));
  }

  @Test
  public void getAllConnections() {
    List<ConnectionNameResponse> responses = mockedController.getAllConnections();
    assertFalse(responses.isEmpty());
    assertEquals(2, responses.size());
    assertConnectionNameResponse(1L, "My Connection", responses.get(0));
    assertConnectionNameResponse(2L, "My Connection 2", responses.get(1));
    // Team without connections
    when(mockedService.getAllConnections("team2")).thenReturn(Collections.emptyList());
    when(mockedUserProfile.getTeamName()).thenReturn("team2");
    assertTrue(mockedController.getAllConnections().isEmpty());
  }

  @Test
  public void getConnection() {
    ConnectionResponse response;
    response = mockedController.getConnection(1L);
    assertConnectionResponse(1L, "My Connection", "SA_content_1", response);
    response = mockedController.getConnection(2L);
    assertConnectionResponse(2L, "My Connection 2", "SA_content_2", response);
  }

  @Test
  public void deleteConnection() {
    mockedController.deleteConnection(1L);
    Mockito.verify(mockedService, Mockito.timeout(1000).times(1)).deleteConnection(1L, "myTeam");
  }

  private void assertConnectionResponse(
      Long expectedId, String expectedName, String expectedSA, ConnectionResponse response) {
    assertEquals(expectedId, response.getId());
    assertEquals(expectedName, response.getName());
    if (response instanceof ServiceAccountConnectionResponse) {
      assertEquals(expectedSA, ((ServiceAccountConnectionResponse) response).getServiceAccount());
    }
  }

  private void assertConnectionNameResponse(
      Long expectedId, String expectedName, ConnectionNameResponse response) {
    assertEquals(expectedId, response.getId());
    assertEquals(expectedName, response.getName());
  }
}
