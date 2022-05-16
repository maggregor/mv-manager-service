package com.achilio.mvm.service.configuration.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtils {

  private static final Logger logger = LoggerFactory.getLogger(JWTUtils.class);
  private final String jwtSecret;

  public JWTUtils(@Value("${jwt.secret}") String secretKey) {
    this.jwtSecret = secretKey;
  }

  public String decodePayload(String token) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    validateJwtToken(token);
    return mapper.writeValueAsString(
        Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token).getBody());
  }

  public boolean validateJwtToken(String token) {
    try {
      Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }
}
