package com.achilio.mvm.service.configuration;

import javax.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@EnableAutoConfiguration
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${server.cors.allowed-origin}")
  private String allowedOrigin;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(corsFilter(), ChannelProcessingFilter.class)
        .csrf()
        .disable()
        .antMatcher("/api/**");
  }

  @Bean
  protected Filter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin(allowedOrigin);
    config.addAllowedHeader("*");
    config.addAllowedMethod("OPTIONS");
    config.addAllowedMethod("HEAD");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedMethod("PATCH");

    source.registerCorsConfiguration("/api/**", config);

    return new CorsFilter(source);
  }
}
