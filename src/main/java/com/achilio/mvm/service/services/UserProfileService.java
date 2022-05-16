package com.achilio.mvm.service.services;

import com.achilio.mvm.service.configuration.jwt.JWTUtils;
import com.achilio.mvm.service.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService implements UserDetailsService {

  @Autowired JWTUtils jwtUtils;

  @Override
  public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
    return null;
  }

  public UserProfile loadUserByJWTPayload(String jwtPayload) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .readValue(jwtPayload, UserProfile.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
