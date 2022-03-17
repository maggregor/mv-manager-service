package com.achilio.mvm.service.configuration;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class SimpleGoogleCredentialsAuthentication implements Authentication {

  private final String clientId;
  private final String clientSecret;
  private final String accessToken;

  public SimpleGoogleCredentialsAuthentication(
      String clientId, String clientSecret, String accessToken) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.accessToken = accessToken;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public UserCredentials getCredentials() {
    AccessToken accessToken = new AccessToken(this.accessToken, null);
    return UserCredentials.newBuilder()
        .setClientId("")
        .setClientSecret("")
        .setAccessToken(accessToken)
        .build();
  }

  @Override
  public Object getDetails() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public boolean isAuthenticated() {
    return true;
  }

  @Override
  public void setAuthenticated(boolean b) throws IllegalArgumentException {}

  @Override
  public String getName() {
    return null;
  }
}
