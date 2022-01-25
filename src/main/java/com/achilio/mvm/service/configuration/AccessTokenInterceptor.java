package com.achilio.mvm.service.configuration;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccessTokenInterceptor extends OncePerRequestFilter {

  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer";

  @Value("${credentials.google.clientId}")
  private String clientId;

  @Value("${credentials.google.clientSecret}")
  private String clientSecret;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      setUpSpringAuthentication(validateToken(request));
      chain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    }
  }

  private String validateToken(HttpServletRequest request) throws IllegalArgumentException {
    final String authorization = request.getHeader(HEADER);
    if (!authorization.contains(PREFIX)) {
      throw new IllegalArgumentException("Bearer prefix is missing");
    }
    String accessToken = authorization.replace(PREFIX, "").trim();
    if (StringUtils.isEmpty(accessToken)) {
      throw new IllegalArgumentException("Access Token is empty");
    }
    return accessToken;
  }

  private void setUpSpringAuthentication(String accessToken) {
    SimpleGoogleCredentialsAuthentication auth =
        new SimpleGoogleCredentialsAuthentication(clientId, clientSecret, accessToken);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
