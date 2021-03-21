package com.alwaysmart.optimizer.controllers;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class RootController {

	@GetMapping(path = "/", produces = "application/json")
	public String getTable(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
		return user.getRefreshToken().getTokenValue();
	}
}
