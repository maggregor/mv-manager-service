package com.achilio.mvm.service;

import com.achilio.mvm.service.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

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

  @Test
  public void simpleValidation() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    UserProfile user =
        mapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(payload, UserProfile.class);
    Assert.assertEquals("nicolas.guelfi@achilio.com", user.getUsername());
    Assert.assertEquals("nicolas.guelfi@achilio.com", user.getEmail());
    Assert.assertEquals("Nicolas", user.getFirstName());
    Assert.assertEquals("Guelfi", user.getLastName());
    Assert.assertEquals("Nicolas Guelfi", user.getName());
    Assert.assertEquals("achilio.com", user.getTeamName());
  }
}
