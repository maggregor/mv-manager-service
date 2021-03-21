package com.alwaysmart.optimizer.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.alwaysmart.optimizer.TableMetadata;
import com.alwaysmart.optimizer.services.IOptimizerService;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class OptimizerController {

    private static Logger LOGGER = LoggerFactory.getLogger(OptimizerController.class);

    @Autowired
    private IOptimizerService service;

    @GetMapping(path = "/project", produces = "application/json")
    public List<String> getAllProjects() {
        return service.getProjects();
    }

    @GetMapping(path = "/dataset", produces = "application/json")
    public List<String> getAllDatasets(@PathVariable String projectId) {
        return service.getDatasets(projectId);
    }

    @GetMapping(path = "/project/{projectId}/dataset/{datasetName}", produces = "application/json")
    public List<String> getAllTables(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user, @PathVariable String projectId, @PathVariable String datasetName) {
        GoogleCredentials credentials =
                UserCredentials.newBuilder()
                        .setClientId(user.getClientRegistration().getClientId())
                        .setClientSecret(user.getClientRegistration().getClientSecret())
                        .setRefreshToken(user.getRefreshToken().getTokenValue())
                        .build();
        return service.getTables(credentials, projectId, datasetName);
    }

    @GetMapping(path = "/table/{tableId}", produces = "application/json")
    public List<TableMetadata> getTable(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user, @PathVariable String projectId, @PathVariable String datasetName, @PathVariable String tableName) {
        GoogleCredentials credentials =
                UserCredentials.newBuilder()
                        .setClientId(user.getClientRegistration().getClientId())
                        .setClientSecret(user.getClientRegistration().getClientSecret())
                        .setRefreshToken(user.getRefreshToken().getTokenValue())
                        .build();
        return service.getTableMetadata(credentials, projectId, datasetName, tableName);
    }

    @GetMapping("/accesstoken")
    String access(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient user) {
        return user.getAccessToken().getTokenValue();
    }
}
