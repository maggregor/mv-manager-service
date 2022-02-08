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

  @GetMapping(path = "/optimize/{projectId}", produces = "application/json")
  @ApiOperation("Get all optimizations by projectId")
  public List<OptimizationResponse> getAllOptimizationByProject(
      @PathVariable final String projectId) {
    return service.getAllOptimizationByProject(projectId).stream()
        .map(OptimizationResponse::new)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/optimize/{projectId}/{datasetName}", produces = "application/json")
  @ApiOperation("Get all optimizations by datasetName")
  public List<OptimizationResponse> getAllOptimizationByProjectAndDatasetName(
      @PathVariable final String projectId, @PathVariable final String datasetName) {
    return service.getAllOptimizationByProjectAndDataset(projectId, datasetName).stream()
        .map(OptimizationResponse::new)
        .collect(Collectors.toList());
  }


  @GetMapping(
      path = "/optimize/{projectId}/{datasetName}/{optimizationId}",
      produces = "application/json")
  @ApiOperation("Get a single optimization from a project and dataset with its results")
  public OptimizationResultsResponse getOptimizationResults(
      @PathVariable final String projectId,
      @PathVariable final String datasetName,
      @PathVariable final Long optimizationId) {
    Optimization optimization = service.getOptimization(projectId, optimizationId);
    List<OptimizationResult> optimizationResults =
        service.getOptimizationResults(projectId, optimizationId);
    return new OptimizationResultsResponse(optimization, optimizationResults);
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
