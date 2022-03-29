package com.achilio.mvm.service.configuration;

import com.achilio.mvm.service.services.JWTDecoderService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {

  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer";
  @Autowired JWTDecoderService jwtDecoderService;

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
    final String id_token = request.getHeader(HEADER);
    if (id_token == null) {
      throw new NullPointerException("No authorization header");
    }
    if (!id_token.contains(PREFIX)) {
      throw new IllegalArgumentException("Bearer prefix is missing");
    }
    String jwt = id_token.replace(PREFIX, "").trim();
    jwtDecoderService.verifySignature(jwt);
    return jwtDecoderService.decodePayload(jwt);
  }

  private void setUpSpringAuthentication(String jwtPayload) {
    SimpleJWTAuthentication auth = new SimpleJWTAuthentication(jwtPayload);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
