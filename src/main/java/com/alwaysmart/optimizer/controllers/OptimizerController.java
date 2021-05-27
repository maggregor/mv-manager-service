package com.alwaysmart.optimizer.controllers;

import com.alwaysmart.optimizer.entities.Optimization;
import com.alwaysmart.optimizer.services.OptimizerService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    private OptimizerService service;

    @PostMapping(path = "/optimize/{projectId}", produces = "application/json")
    @ApiOperation("Trigger an optimization on a projectId")
    public OptimizationResponse optimizeProject(@PathVariable("projectId") String projectId) {
        Optimization optimization = service.optimizeProject(projectId);
        return new OptimizationResponse(optimization.getId(), optimization.getProjectId());
    }

}
