package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ConnectionController;
import com.achilio.mvm.service.controllers.ConnectionNameResponse;
import com.achilio.mvm.service.controllers.requests.ConnectionResponse;
import com.achilio.mvm.service.entities.Connection;
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

  @Before
  public void setup() {
    // Mock security context for the getContextTeamName() method in Controller
    Authentication mockedAuth = mock(Authentication.class);
    SecurityContext mockedSecurityContext = mock(SecurityContext.class);
    UserProfile mockedUserProfile = mock(UserProfile.class);
    when(mockedUserProfile.getTeamName()).thenReturn("myTeam");
    when(mockedAuth.getDetails()).thenReturn(mockedUserProfile);
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
    //
    Connection connection = mock(ServiceAccountConnection.class);
    when(connection.getId()).thenReturn(1L);
    when(connection.getName()).thenReturn("My Connection");
    Connection connection2 = mock(ServiceAccountConnection.class);
    when(connection2.getId()).thenReturn(2L);
    when(connection2.getName()).thenReturn("My Connection 2");
    when(mockedService.getAllConnections(mockedUserProfile.getTeamName()))
        .thenReturn(Arrays.asList(connection, connection2));
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
    assertTrue(responses.isEmpty());
  }

  @Test
  public void getConnection() {
    ConnectionResponse response = mockedController.getConnection(1L);
    assertConnectionNameResponse(1L, "My Connection", response);
  }

  private void assertConnectionResponse(
      Long expectedId, String expectedName, ConnectionNameResponse response) {
    assertEquals(expectedId, response.getId());
    assertEquals(expectedName, response.getName());
  }

  private void assertConnectionNameResponse(
      Long expectedId, String expectedName, ConnectionNameResponse response) {
    assertEquals(expectedId, response.getId());
    assertEquals(expectedName, response.getName());
  }
}
