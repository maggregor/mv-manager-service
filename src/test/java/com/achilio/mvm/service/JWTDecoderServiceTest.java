package com.achilio.mvm.service;

import com.achilio.mvm.service.services.JWTDecoderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTDecoderServiceTest {

  private final String token1 =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Im5pY29sYXMuZ3VlbGZpQGFjaGlsaW8uY29tIiwiZW1haWwiOiJuaWNvbGFzLmd1ZWxmaUBhY2hpbGlvLmNvbSIsImZpcnN0X25hbWUiOiJOaWNvbGFzIiwibGFzdF9uYW1lIjoiR3VlbGZpIiwibmFtZSI6Ik5pY29sYXMgR3VlbGZpIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdpVldEZ1Rod3VkanZ5cUpKSG5wZXdScXJTaWdoZ05fTkh6N3VGaD1zOTYtYyIsImlhdCI6MTY0ODMyNzUyMSwiZXhwIjoxNjUwOTU1NTIxLCJqdGkiOiJkODE4ZjYyOC1jMDdmLTQzOWUtOTMzYS1lMzBhODJjM2E0ODgiLCJ1c2VyX2lkIjoyLCJvcmlnX2lhdCI6MTY0ODMyNzUyMSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDAwIn0.06ot55ASYKQwCLbkNHXFPvb4qhPHYz0XtZujUopRQQc";
  private final String token2 =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Im5pY29sYXMuZ3VlbGZpQGFjaGlsaW8uY29tIiwiaWF0IjoxNjQ4MzEzMDM5LCJleHAiOjE2NTA5NDEwMzksImp0aSI6IjUzNWEwMWQ3LTU0YzctNDQ3Yi05ZDIwLWFkMmZhMGVlYmIwZSIsInVzZXJfaWQiOjIsIm9yaWdfaWF0IjoxNjQ4MzEzMDM5fQ.VSGaL308XjmgiuHmagxm3S60xh5Zib_dxq8FQPS7uvU";
  private final String token3 =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Im5pY29sYXMuZ3VlbGZpQGFjaGlsaW8uY29tIiwiZW1haWwiOiJuaWNvbGFzLmd1ZWxmaUBhY2hpbGlvLmNvbSIsImlhdCI6MTY0ODMyMzA5MywiZXhwIjoxNjUwOTUxMDkzLCJqdGkiOiJlZGRhMDcyOS02OGUzLTQxMjctOWE1NS04MGVlYjA3YTY5YTEiLCJ1c2VyX2lkIjoyLCJvcmlnX2lhdCI6MTY0ODMyMzA5M30.Iso0hCEO8u2n2XDH1D0O7Ym3OjMh7kaH_hEVHFRM7d0";
  @Autowired private JWTDecoderService decoder = new JWTDecoderService("secret");

  @Before
  public void setup() {
    decoder.readToken(token1);
  }

  @Test
  public void decodeHeaderTest() {
    String header = decoder.decodeHeader();
    String expected = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
    Assert.assertEquals(expected, decoder.getHeader());
    Assert.assertEquals(expected, header);
  }

  @Test
  public void decodePayloadTest() {
    String payload = decoder.decodePayload();
    String expected =
        "{\"username\":\"nicolas.guelfi@achilio.com\",\"email\":\"nicolas.guelfi@achilio.com\",\"first_name\":\"Nicolas\",\"last_name\":\"Guelfi\",\"name\":\"Nicolas Guelfi\",\"picture\":\"https://lh3.googleusercontent.com/a-/AOh14GiVWDgThwudjvyqJJHnpewRqrSighgN_NHz7uFh=s96-c\",\"iat\":1648327521,\"exp\":1650955521,\"jti\":\"d818f628-c07f-439e-933a-e30a82c3a488\",\"user_id\":2,\"orig_iat\":1648327521,\"iss\":\"http://localhost:8000\"}";
    Assert.assertEquals(expected, decoder.getPayload());
    Assert.assertEquals(expected, payload);
  }

  @Test
  public void verifySignatureToken1() {
    Assert.assertTrue(decoder.verifySignature());
  }
}
