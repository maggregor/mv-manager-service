package com.achilio.mvm.service;

import com.achilio.mvm.service.services.JWTDecoderService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTDecoderServiceTest {

  private final String token1 =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Im5pY29sYXMuZ3VlbGZpQGFjaGlsaW8uY29tIiwiZW1haWwiOiJuaWNvbGFzLmd1ZWxmaUBhY2hpbGlvLmNvbSIsImZpcnN0X25hbWUiOiJOaWNvbGFzIiwibGFzdF9uYW1lIjoiR3VlbGZpIiwibmFtZSI6Ik5pY29sYXMgR3VlbGZpIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdpVldEZ1Rod3VkanZ5cUpKSG5wZXdScXJTaWdoZ05fTkh6N3VGaD1zOTYtYyIsImhkIjoiYWNoaWxpby5jb20iLCJpYXQiOjE2NDg0OTY4MzYsImV4cCI6MTY1MTEyNDgzNiwianRpIjoiMTJkMzkzYTktMDkxZS00NTdjLWI4MjItZGJjMDg1NmZjOGVlIiwidXNlcl9pZCI6NSwib3JpZ19pYXQiOjE2NDg0OTY4MzYsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCJ9.LDfXoBJRmxR5NZzb_XtYDGFglbmQlklVACn4DkwVcg4";
  @Autowired private JWTDecoderService decoder = new JWTDecoderService("secret");

  @Test
  public void decodeHeaderTest() {
    String expected = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
    Assert.assertEquals(expected, decoder.decodeHeader(token1));
  }

  @Test
  public void decodePayloadTest() {
    String expected =
        "{\"username\":\"nicolas.guelfi@achilio.com\","
            + "\"email\":\"nicolas.guelfi@achilio.com\","
            + "\"first_name\":\"Nicolas\","
            + "\"last_name\":\"Guelfi\","
            + "\"name\":\"Nicolas Guelfi\","
            + "\"picture\":\"https://lh3.googleusercontent.com/a-/AOh14GiVWDgThwudjvyqJJHnpewRqrSighgN_NHz7uFh=s96-c\","
            + "\"hd\":\"achilio.com\","
            + "\"iat\":1648496836,"
            + "\"exp\":1651124836,"
            + "\"jti\":\"12d393a9-091e-457c-b822-dbc0856fc8ee\","
            + "\"user_id\":5,"
            + "\"orig_iat\":1648496836,"
            + "\"iss\":\"http://localhost:8000\"}";
    Assert.assertEquals(expected, decoder.decodePayload(token1));
  }

  @Test
  public void verifySignatureToken1() {
    Assert.assertTrue(decoder.verifySignature(token1));
  }

  @Test
  public void verifySignatureWrongSecret() {
    JWTDecoderService decoderWrongSecret = new JWTDecoderService("wrongSecret");
    Assert.assertFalse(decoderWrongSecret.verifySignature(token1));
  }
}
