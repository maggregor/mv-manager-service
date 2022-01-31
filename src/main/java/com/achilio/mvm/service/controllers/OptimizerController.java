package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.responses.OptimizationResponse;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.services.OptimizerService;
import io.swagger.annotations.ApiOperation;
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

  @Autowired
  private OptimizerService service;

  @PostMapping(path = "/optimize/{projectId}", produces = "application/json")
  @ApiOperation("Trigger an optimization on a projectId")
  @Deprecated
  public OptimizationResponse optimizeProject(@PathVariable("projectId") String projectId)
      throws Exception {
    Optimization optimization = service.optimizeProject(projectId);
    return new OptimizationResponse(optimization);
  }

  @PostMapping(path = "/optimize/{projectId}/dataset/{datasetName}", produces = "application/json")
  @ApiOperation("Trigger an optimization on a projectId")
  public OptimizationResponse optimizeProject(@PathVariable("projectId") String projectId,
      @PathVariable("datasetName") String datasetName)
      throws Exception {
    Optimization optimization = service.optimizeDataset(projectId, datasetName);
    return new OptimizationResponse(optimization);
  }
}
