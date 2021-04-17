package com.alwaysmart.optimizer.controllers;

import java.util.Collection;
import java.util.List;

import com.alwaysmart.optimizer.DatasetMetadata;
import com.alwaysmart.optimizer.ProjectMetadata;
import com.alwaysmart.optimizer.TableMetadata;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.services.IOptimizerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping(path = "/project/{projectId}", produces = "application/json")
    public ProjectMetadata getProject(@PathVariable String projectId) {
        return service.getProject(projectId);
    }

    @GetMapping(path = "/dataset/{datasetId}", produces = "application/json")
    public DatasetMetadata getDataset(@PathVariable String datasetId) {
        return service.getDataset(datasetId);
    }

    @GetMapping(path = "/table/{tableId}", produces = "application/json")
    public TableMetadata getTable(@PathVariable String tableId) {
        return service.getTableMetadata(tableId);
    }

    @GetMapping(path = "/project/{projectName}/optimize", produces = "application/json")
    public Collection<FieldSet> optimizeProject(@PathVariable String projectName) {
        return service.optimizeProject(projectName);
    }

}
