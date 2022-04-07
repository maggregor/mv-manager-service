package com.achilio.mvm.service.services;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTDecoderService {

  private final String SECRET_KEY;
  private final Base64.Decoder decoder = Base64.getUrlDecoder();

  public JWTDecoderService(@Value("${jwt.secret}") String secretKey) {
    this.SECRET_KEY = secretKey;
  }

  public String decodeHeader(String token) {
    String[] chunks = getChunks(token);
    verifySignature(token);
    return new String(decoder.decode(chunks[0]));
  }

  public String decodePayload(String token) {
    String[] chunks = getChunks(token);
    verifySignature(token);
    return new String(decoder.decode(chunks[1]));
  }

  public boolean verifySignature(String token) {
    String[] chunks = getChunks(token);
    SignatureAlgorithm sa = HS256;
    SecretKeySpec secretKeySpec = new SecretKeySpec(this.SECRET_KEY.getBytes(), sa.getJcaName());
    DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
    return validator.isValid(chunks[0] + "." + chunks[1], chunks[2]);
  }

  private String[] getChunks(String token) {
    return token.split("\\.");
  }
}
