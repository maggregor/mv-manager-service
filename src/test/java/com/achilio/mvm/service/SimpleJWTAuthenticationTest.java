package com.achilio.mvm.service;

import com.achilio.mvm.service.configuration.SimpleJWTAuthentication;
import com.achilio.mvm.service.models.UserProfile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleJWTAuthenticationTest {
  private String payload =
      "{"
          + "\"username\":\"nicolas.guelfi@achilio.com\","
          + "\"email\":\"nicolas.guelfi@achilio.com\","
          + "\"first_name\":\"Nicolas\","
          + "\"last_name\":\"Guelfi\","
          + "\"name\":\"Nicolas Guelfi\","
          + "\"picture\":\"https://lh3.googleusercontent.com/a-/AOh14GiVWDgThwudjvyqJJHnpewRqrSighgN_NHz7uFh=s96-c\","
          + "\"hd\":\"achilio.com\","
          + "\"iat\":1648463675,"
          + "\"exp\":1651091675,"
          + "\"jti\":\"7cb6be8f-1630-44a5-8aaf-a7bc3eaa103c\","
          + "\"user_id\":2,"
          + "\"orig_iat\":1648463675,"
          + "\"iss\":\"https://dev.auth.achilio.com\""
          + "}";

  private String wrongPayload =
      "{"
          + "\"username\":\"nicolas.guelfi@achilio.com\","
          + "\"email\":\"nicolas.guelfi@achilio.com\","
          + "\"first_name\":\"Nicolas\","
          + "\"last_name\":\"Guelfi\","
          + "\"name\":\"Nicolas Guelfi\""
          + "\"picture\":\"https://lh3.googleusercontent.com/a-/AOh14GiVWDgThwudjvyqJJHnpewRqrSighgN_NHz7uFh=s96-c\","
          + "\"hd\":\"achilio.com\","
          + "\"iat\":1648463675,"
          + "\"exp\":1651091675,"
          + "\"jti\":\"7cb6be8f-1630-44a5-8aaf-a7bc3eaa103c\","
          + "\"user_id\":2,"
          + "\"orig_iat\":1648463675,"
          + "\"iss\":\"https://dev.auth.achilio.com\""
          + "}";

  @Test
  public void gettersValidationTest() {
    SimpleJWTAuthentication jwtAuthentication = new SimpleJWTAuthentication(payload);
    Assert.assertEquals("nicolas.guelfi@achilio.com", jwtAuthentication.getPrincipal());
    Assert.assertEquals("Nicolas Guelfi", jwtAuthentication.getName());
  }

  @Test
  public void illegalArgumentTest() {
    Assert.assertThrows(
        IllegalArgumentException.class, () -> new SimpleJWTAuthentication(wrongPayload));
  }

  @Test
  public void getDetailsTest() {
    SimpleJWTAuthentication jwtAuthentication = new SimpleJWTAuthentication(payload);
    UserProfile expectedUser =
        new UserProfile(
            "nicolas.guelfi@achilio.com",
            "nicolas.guelfi@achilio.com",
            "Nicolas",
            "Guelfi",
            "Nicolas Guelfi",
            "achilio.com");
    Assert.assertEquals(expectedUser, jwtAuthentication.getDetails());
    expectedUser =
        new UserProfile(
            "nicolas.guelfi@achilio.com",
            "nicolas.guelfi@achilio.com",
            "Nicolas",
            "Guelfi",
            "Nicolas Guelf",
            "achilio.com");
    Assert.assertNotEquals(expectedUser, jwtAuthentication.getDetails());
  }
}
