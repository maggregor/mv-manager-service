package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.configuration.jwt.JWTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

public class JWTUtilsTest {

  private final String tokenValid =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o";

  private static final JWTUtils jwtUtils = new JWTUtils("secret");
  private static final JWTUtils jwtUtils1 = new JWTUtils("wrongKey");

  @Test
  public void decodePayload() throws JsonProcessingException {
    String expected = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}";
    String actual = jwtUtils.decodePayload(tokenValid);
    assertEquals(expected, actual);
  }

  @Test
  public void validateJwtToken() {
    assertTrue(jwtUtils.validateJwtToken(tokenValid));
  }

  @Test
  public void validateJwtToken__whenSignatureInvalid_false() {
    String tokenInvalidSignature =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2";
    assertFalse(jwtUtils.validateJwtToken(tokenInvalidSignature));
    assertFalse(jwtUtils1.validateJwtToken(tokenValid));
  }

  @Test
  public void validateJwtToken__whenMalformedJwt_false() {
    String invalidToken = "asdasd";
    assertFalse(jwtUtils.validateJwtToken(invalidToken));
  }

  @Test
  public void validateJwtToken__whenExpiredJwt_false() {
    String expiredToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1NTU1NTU1NTV9.Zjltk2kKUhb8sOXf9S-FU3UOTmqIB3HN0G8hDxa11vc";
    assertFalse(jwtUtils.validateJwtToken(expiredToken));
  }

  @Test
  public void validateJwtToken__whenEmptyJwt_false() {
    String emptyToken = "";
    assertFalse(jwtUtils.validateJwtToken(emptyToken));
  }

  @Test
  public void validateJwtToken__whenUnsigned_false() {
    String unsignedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.";
    assertFalse(jwtUtils.validateJwtToken(unsignedToken));
  }
}
