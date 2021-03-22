package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.TableMetadata;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class OptimizerService implements IOptimizerService {

    @Autowired
    OAuth2AuthorizedClientService clientService;

    @Override
    public List<String> getProjects() {
        return null;
    }

    @Override
    public List<String> getDatasets(String projectId) {
        return null;
    }

    @Override
    public List<String> getTables(String projectId, String datasetName) {
        return new BigQueryDatabaseFetcher(buildGoogleCredentials()).getTables(projectId, datasetName);
    }

    @Override
    public TableMetadata getTableMetadata(String projectId, String datasetName, String tableName) {
        return new BigQueryDatabaseFetcher(buildGoogleCredentials()).fetchTableMetadata(projectId, datasetName, tableName);
    }

    private GoogleCredentials buildGoogleCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
        AccessToken accessToken = new AccessToken(client.getAccessToken().getTokenValue(), Date.from(client.getAccessToken().getExpiresAt()));
        return UserCredentials.newBuilder()
                        .setClientId(client.getClientRegistration().getClientId())
                        .setClientSecret(client.getClientRegistration().getClientSecret())
                        .setAccessToken(accessToken)
                        .build();
    }
}