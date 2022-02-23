package com.achilio.mvm.service.configuration;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.exceptions.UnauthorizedException;
import com.google.cloud.resourcemanager.ResourceManagerException;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class GoogleProjectInterceptor extends HandlerInterceptorAdapter {

  private static Logger LOGGER = LoggerFactory.getLogger(GoogleProjectInterceptor.class);

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    Map map =
        new TreeMap<>(
            (Map<String, String>)
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
    if (map.containsKey("projectId")) {
      String projectId = (String) map.get("projectId");
      try {
        SimpleGoogleCredentialsAuthentication authentication =
            (SimpleGoogleCredentialsAuthentication)
                SecurityContextHolder.getContext().getAuthentication();
        new BigQueryDatabaseFetcher(authentication.getCredentials(), projectId);
      } catch (ResourceManagerException e) {
        throw new UnauthorizedException("Access token is invalid");
      }
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable Exception ex)
      throws Exception {}
}
