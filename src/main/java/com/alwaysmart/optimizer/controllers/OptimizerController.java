package com.alwaysmart.optimizer.controllers;

import java.util.List;

import com.alwaysmart.optimizer.TableMetadata;
import com.alwaysmart.optimizer.services.IOptimizerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class OptimizerController {
    private Logger LOGGER = LoggerFactory.getLogger(UserController.class);

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

    @GetMapping(path = "/table", produces = "application/json")
    public List<String> getAllTables(@PathVariable String projectId, @PathVariable String datasetName) {
        return service.getTables(projectId, datasetName);
    }

    @GetMapping(path = "/table/{tableId}", produces = "application/json")
    public List<TableMetadata> getTable(@PathVariable String projectId, @PathVariable String datasetName, @PathVariable String tableName) {
        return service.getTableMetadata(projectId, datasetName, tableName);
    }
}
