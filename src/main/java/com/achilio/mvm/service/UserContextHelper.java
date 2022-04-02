package com.achilio.mvm.service;

import com.achilio.mvm.service.models.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContextHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserContextHelper.class);

  public static UserProfile getUserProfile() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication == null) {
      LOGGER.error("Authentication is security context is null");
      throw new IllegalArgumentException("Can't retrieve authentication");
    }
    Object details = authentication.getDetails();
    if (details == null) {
      LOGGER.error("Auth details in the security context is null");
      throw new IllegalArgumentException("Can't retrieve user profile");
    } else if (!(details instanceof UserProfile)) {
      LOGGER.error("Auth details in the security context is not an instance of UserProfile");
      throw new IllegalArgumentException("Can't retrieve user profile");
    }
    return (UserProfile) details;
  }

  public static String getContextTeamName() {
    return getUserProfile().getTeamName();
  }

  public static String getContextEmail() {
    return getUserProfile().getEmail();
  }

  public static String getContextUsername() {
    return getUserProfile().getUsername();
  }
}
