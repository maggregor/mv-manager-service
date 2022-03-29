package com.achilio.mvm.service.configuration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

/**
 * By default use of @MatrixVariable is disabled in Spring MVC. To use these variables we have to
 * manually enable them by implementing below class.
 */
@Configuration
@EnableAutoConfiguration
public class WebConfig implements WebMvcConfigurer {

  @Autowired JWTInterceptor jwtInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(jwtInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/api/v1/webhook/**");
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    UrlPathHelper urlPathHelper = new UrlPathHelper();
    urlPathHelper.setRemoveSemicolonContent(false);
    configurer.setUrlPathHelper(urlPathHelper);
  }

  @Bean
  public LinkDiscoverers discoverers() {
    List<LinkDiscoverer> plugins = new ArrayList<>();
    plugins.add(new CollectionJsonLinkDiscoverer());
    return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
  }
}
