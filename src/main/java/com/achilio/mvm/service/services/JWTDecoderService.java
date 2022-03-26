package com.achilio.mvm.service.services;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTDecoderService {

  private Base64.Decoder decoder = Base64.getUrlDecoder();
  private String[] chunks;
  private String tokenWithoutSignature;
  private String signature;
  private String header;
  private String payload;

  private String SECRET_KEY;

  public JWTDecoderService(@Value("${jwt.secret}") String secretKey) {
    this.SECRET_KEY = secretKey;
  }

  public void readToken(String token) {
    this.chunks = token.split("\\.");
    this.tokenWithoutSignature = this.chunks[0] + "." + this.chunks[1];
    this.signature = this.chunks[2];
  }

  public String decodeHeader() {
    this.header = new String(decoder.decode(this.chunks[0]));
    return header;
  }

  public String decodePayload() {
    this.payload = new String(decoder.decode(this.chunks[1]));
    return payload;
  }

  public boolean verifySignature() {
    SignatureAlgorithm sa = HS256;
    SecretKeySpec secretKeySpec = new SecretKeySpec(this.SECRET_KEY.getBytes(), sa.getJcaName());
    DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
    return validator.isValid(this.tokenWithoutSignature, this.signature);
  }

  public String getHeader() {
    return header;
  }

  public String getPayload() {
    return payload;
  }

  public String getSignature() {
    return signature;
  }
}
