package com.alwaysmart.optimizer.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class BasicAuthenticationHandler extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
				http
				.httpBasic().and().authorizeRequests()
				.antMatchers(HttpMethod.GET, "/api").hasRole("USER")
				.and()
				.csrf()
				.disable()
				.formLogin()
				.disable();

	}
}