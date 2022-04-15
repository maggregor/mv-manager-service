package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.models.UserProfile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class UserContextHelperTest {

  private static final UserProfile USER_PROFILE_1 =
      new UserProfile(
          "moi", "moi@achilio.com", "foo", "bar", "myName", "myTeamName", "customer-id", null);
  @Mock private Authentication mockedJWTAuth;
  @Mock private SecurityContext securityContext;

  @Before
  public void setup() {
    when(securityContext.getAuthentication()).thenReturn(mockedJWTAuth);
    when(mockedJWTAuth.getPrincipal()).thenReturn(USER_PROFILE_1);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void getUserProfile() {
    assertEquals(USER_PROFILE_1, UserContextHelper.getUserProfile());
  }

  @Test
  public void getContextStaticMethods() {
    assertEquals(USER_PROFILE_1.getTeamName(), UserContextHelper.getContextTeamName());
    assertEquals(USER_PROFILE_1.getEmail(), UserContextHelper.getContextEmail());
    assertEquals(USER_PROFILE_1.getCustomerId(), UserContextHelper.getContextStripeCustomerId());
    assertEquals(USER_PROFILE_1.getUsername(), UserContextHelper.getContextUsername());
  }

  @Test
  public void when_authIsNull_throwException() {
    when(securityContext.getAuthentication()).thenReturn(null);
    Exception e = assertThrows(IllegalArgumentException.class, UserContextHelper::getUserProfile);
    assertEquals(e.getMessage(), "Can't retrieve authentication");
  }

  @Test
  public void when_detailsIsNotInstanceOfUserProfile_throwException() {
    when(mockedJWTAuth.getPrincipal()).thenReturn(new Object()); // Can be another details object
    Exception e = assertThrows(IllegalArgumentException.class, UserContextHelper::getUserProfile);
    assertEquals(e.getMessage(), "Can't retrieve user profile");
  }

  @Test
  public void when_detailsIsNull_throwException() {
    when(mockedJWTAuth.getPrincipal()).thenReturn(null);
    Exception e = assertThrows(IllegalArgumentException.class, UserContextHelper::getUserProfile);
    assertEquals(e.getMessage(), "Can't retrieve user profile");
  }
}
