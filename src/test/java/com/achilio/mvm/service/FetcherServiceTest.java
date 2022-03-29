package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.services.ConnectionService;
import com.achilio.mvm.service.services.FetcherService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class FetcherServiceTest {

  @InjectMocks FetcherService fetcherService;
  @Mock ConnectionService connectionService;
  @Mock ServiceAccountConnection serviceAccountConnection1;
  @Mock ServiceAccountConnection serviceAccountConnection2;

  private String CONNECTION1_CONTENT = "JSON_CONTENT1";
  private String CONNECTION2_CONTENT = "JSON_CONTENT2";
  private String TEAM_1 = "Team1";
  private String TEAM_2 = "Team2";
  private UserProfile USER_1 =
      new UserProfile("myUsername", "myEmail", "myFirstName", "myLastName", "myName", TEAM_1);
  @Mock private Authentication mockedJWTAuth;
  @Mock private SecurityContext securityContext;

  @Before
  public void setup() {
    when(securityContext.getAuthentication()).thenReturn(mockedJWTAuth);
    SecurityContextHolder.setContext(securityContext);
    when(connectionService.getAllConnections(any()))
        .thenReturn(Arrays.asList(serviceAccountConnection1, serviceAccountConnection2));
    when(serviceAccountConnection1.getContent()).thenReturn(CONNECTION1_CONTENT);
    when(mockedJWTAuth.getDetails()).thenReturn(USER_1);
  }

  @Test
  public void when_connectionIsTwo_thenReturnsConnection1() {
    ServiceAccountConnection sa = fetcherService.getSAAvailableConnection();
    Assert.assertEquals(CONNECTION1_CONTENT, sa.getContent());
  }

  @Test
  public void when_connectionIsOne_thenReturnsConnection1() {
    when(connectionService.getAllConnections(any()))
        .thenReturn(Collections.singletonList(serviceAccountConnection1));
    ServiceAccountConnection sa = fetcherService.getSAAvailableConnection();
    Assert.assertEquals(CONNECTION1_CONTENT, sa.getContent());
  }

  @Test
  public void when_connectionIsEmpty_thenThrowIllegalArgumentException() {
    when(connectionService.getAllConnections(any())).thenReturn(Collections.emptyList());
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> fetcherService.getSAAvailableConnection());
    assertEquals(e.getMessage(), "Can't initialize the fetcher: no connection found");
  }
}
