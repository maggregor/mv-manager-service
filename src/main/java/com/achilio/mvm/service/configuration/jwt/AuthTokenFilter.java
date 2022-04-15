package com.achilio.mvm.service.configuration.jwt;

import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.services.UserProfileService;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenFilter.class);
  @Autowired private JWTUtils jwtUtils;
  @Autowired private UserProfileService userProfileService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        UserProfile userProfile =
            userProfileService.loadUserByJWTPayload(jwtUtils.decodePayload(jwt));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userProfile, null, userProfile.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        throw new AccessDeniedException("access is denied");
      }
    } catch (Exception e) {
      LOGGER.error("Cannot set user authentication: ", e);
      throw e;
    }
    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    Cookie jwtCookie =
        Arrays.stream(request.getCookies())
            .filter(c -> c.getName().equals("jwt_token"))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    return jwtCookie.getValue();
  }
}
