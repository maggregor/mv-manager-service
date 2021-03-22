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
import org.springframework.http.ResponseEntity;
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
    public List<String> getAllTables(@PathVariable String projectId, @PathVariable String datasetName) {
        return service.getTables(projectId, datasetName);
    }

    @GetMapping(path = "/project/{projectId}/dataset/{datasetName}/table/{tableName}", produces = "application/json")
    public ResponseEntity<TableMetadata> getTable(@PathVariable String projectId, @PathVariable String datasetName, @PathVariable String tableName) {
        return ResponseEntity.ok(service.getTableMetadata(projectId, datasetName, tableName));
    }
}
