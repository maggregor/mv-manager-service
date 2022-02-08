package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.responses.OptimizationResponse;
import com.achilio.mvm.service.controllers.responses.OptimizationResultsResponse;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.services.OptimizerService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
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

  @Autowired
  private OptimizerService service;

  @GetMapping(path = "/optimize/{optimizationId}", produces = "application/json")
  @ApiOperation("Get the details of an optimizeId")
  public OptimizationResponse getOptimization(@PathVariable final Long optimizationId) {
    Optimization optimization = service.getOptimization(optimizationId);
    return new OptimizationResponse(optimization);
  }

  @GetMapping(path = "/optimize/project/{projectId}", produces = "application/json")
  @ApiOperation("Get the details of an optimizeId")
  public List<OptimizationResponse> getOptimizations(@PathVariable final String projectId) {
    return service.getAllOptimizations(projectId)
        .stream()
        .map(OptimizationResponse::new)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/optimize/{optimizationId}/results", produces = "application/json")
  @ApiOperation("Get all the results of an optimizeId")
  public OptimizationResultsResponse getOptimizationResults(
      @PathVariable final Long optimizationId) {
    Optimization optimization = service.getOptimization(optimizationId);
    List<OptimizationResult> optimizationResults = service.getOptimizationResults(optimizationId);
    OptimizationResultsResponse o =
        new OptimizationResultsResponse(optimization, optimizationResults);
    return o;
  }

  @PostMapping(path = "/optimize/{projectId}/dataset/{datasetName}", produces = "application/json")
  @ApiOperation("Trigger an optimization on a projectId")
  public OptimizationResponse optimizeProject(
      @PathVariable("projectId") String projectId, @PathVariable("datasetName") String datasetName)
      throws Exception {
    Optimization optimization = service.optimizeDataset(projectId, datasetName);
    return new OptimizationResponse(optimization);
  }
}
