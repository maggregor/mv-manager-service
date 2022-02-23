package com.achilio.mvm.service.configuration;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class AccessTokenInterceptor extends HandlerInterceptorAdapter {
  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer";
  private static Logger LOGGER = LoggerFactory.getLogger(GoogleProjectInterceptor.class);

  @Value("${credentials.google.clientId}")
  private String clientId;

  @Value("${credentials.google.clientSecret}")
  private String clientSecret;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    try {
      setUpSpringAuthentication(validateToken(request));
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    }
    return true;
  }

  private String validateToken(HttpServletRequest request)
      throws IllegalArgumentException, NullPointerException {
    final String authorization = request.getHeader(HEADER);
    if (authorization == null) {
      throw new NullPointerException("No authorization header");
    }
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
