package com.achilio.mvm.service.configuration;

import com.achilio.mvm.service.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class SimpleJWTAuthentication implements Authentication {

  private String jwtPayload;
  private UserProfile user;

  public SimpleJWTAuthentication(String jwtPayload) {
    this.user = setUserFromPayload(jwtPayload);
    this.jwtPayload = jwtPayload;
  }

  private UserProfile setUserFromPayload(String jwtPayload) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .readValue(jwtPayload, UserProfile.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public UserProfile getDetails() {
    return user;
  }

  @Override
  public Object getPrincipal() {
    return user.getUsername();
  }

  @Override
  public String getName() {
    return user.getName();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public boolean isAuthenticated() {
    return false;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
}
