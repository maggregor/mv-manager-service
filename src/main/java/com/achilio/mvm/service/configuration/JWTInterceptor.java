package com.achilio.mvm.service.configuration;

import com.achilio.mvm.service.services.JWTDecoderService;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(JWTInterceptor.class);

  @Autowired JWTDecoderService jwtDecoderService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    try {
      Cookie jwtToken =
          Arrays.stream(request.getCookies())
              .filter(c -> c.getName().equals("jwt_token"))
              .findFirst()
              .orElseThrow(IllegalArgumentException::new);
      setUpSpringAuthentication(validateToken(jwtToken));
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    }
    return true;
  }

  private String validateToken(Cookie jwtToken)
      throws IllegalArgumentException, NullPointerException {
    if (!jwtDecoderService.verifySignature(jwtToken.getValue())) {
      LOGGER.error("JWT {} signature invalid", jwtToken);
    }
    ;
    return jwtDecoderService.decodePayload(jwtToken.getValue());
  }

  private void setUpSpringAuthentication(String jwtPayload) {
    SimpleJWTAuthentication auth = new SimpleJWTAuthentication(jwtPayload);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
