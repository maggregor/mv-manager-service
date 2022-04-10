package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ConnectionController;
import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.controllers.responses.ConnectionResponse;
import com.achilio.mvm.service.controllers.responses.ServiceAccountConnectionResponse;
import com.achilio.mvm.service.entities.Connection.SourceType;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.services.ConnectionService;
import java.io.IOException;
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

  private static final ConnectionRequest REQUEST1 =
      new ServiceAccountConnectionRequest("My Connection", SourceType.BIGQUERY, "content");

  @InjectMocks ConnectionController controller;
  @Mock ConnectionService mockedService;
  @Mock private UserProfile mockedUserProfile;

  @Before
  public void setup() throws IOException {
    // Mock security context for the getContextTeamName() method in Controller
    Authentication mockedAuth = mock(Authentication.class);
    SecurityContext mockedSecurityContext = mock(SecurityContext.class);
    when(mockedUserProfile.getTeamName()).thenReturn("myTeam");
    when(mockedAuth.getDetails()).thenReturn(mockedUserProfile);
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
    //
    ServiceAccountConnection mockedConnection1 = mock(ServiceAccountConnection.class);
    ServiceAccountConnection mockedConnection2 = mock(ServiceAccountConnection.class);
    when(mockedConnection1.getId()).thenReturn(1L);
    when(mockedConnection1.getName()).thenReturn("My Connection");
    when(mockedConnection2.getId()).thenReturn(2L);
    when(mockedConnection2.getName()).thenReturn("My Connection 2");
    when(mockedService.getConnection(1L, "myTeam")).thenReturn(mockedConnection1);
    when(mockedService.getConnection(2L, "myTeam")).thenReturn(mockedConnection2);
    when(mockedService.getAllConnections(mockedUserProfile.getTeamName()))
        .thenReturn(Arrays.asList(mockedConnection1, mockedConnection2));
    when(mockedService.createConnection(any(), any(), any())).thenReturn(mockedConnection1);
    when(mockedService.updateConnection(eq(1L), anyString(), eq(REQUEST1)))
        .thenReturn(mockedConnection1);
    doNothing().when(mockedService).uploadConnectionToGCS(any());
  }

  @Test
  public void getAllConnections() {
    List<ConnectionResponse> responses = controller.getAllConnections();
    assertFalse(responses.isEmpty());
    assertEquals(2, responses.size());
    assertConnectionResponse(1L, "My Connection", responses.get(0));
    assertConnectionResponse(2L, "My Connection 2", responses.get(1));
    // Team without connections
    when(mockedService.getAllConnections("team2")).thenReturn(Collections.emptyList());
    when(mockedUserProfile.getTeamName()).thenReturn("team2");
    assertTrue(controller.getAllConnections().isEmpty());
  }

  @Test
  public void getConnection() {
    ConnectionResponse response;
    response = controller.getConnection(1L);
    assertConnectionResponse(1L, "My Connection", response);
    response = controller.getConnection(2L);
    assertConnectionResponse(2L, "My Connection 2", response);
  }

  @Test
  public void createConnection() {
    ConnectionResponse response;
    response = controller.createConnection(REQUEST1);
    assertConnectionResponse(1L, "My Connection", response);
  }

  @Test
  public void createConnection__whenConnectionNull_throwException() {

    ConnectionResponse response;
    response = controller.createConnection(REQUEST1);
    assertConnectionResponse(1L, "My Connection", response);
  }

  @Test
  public void updateConnection() {
    ConnectionResponse response;
    response = controller.updateConnection(1L, REQUEST1);
    assertConnectionResponse(1L, "My Connection", response);
  }

  @Test
  public void deleteConnection() {
    controller.deleteConnection(1L);
    Mockito.verify(mockedService, Mockito.timeout(1000).times(1)).deleteConnection(1L, "myTeam");
  }

  private void assertConnectionResponse(
      Long expectedId, String expectedName, ConnectionResponse response) {
    assertEquals(expectedId, response.getId());
    assertEquals(expectedName, response.getName());
    assertEquals("secretkey", ((ServiceAccountConnectionResponse) response).getContent());
  }
}
