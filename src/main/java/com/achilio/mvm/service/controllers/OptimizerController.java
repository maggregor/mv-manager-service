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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class OptimizerController {

  @Autowired private OptimizerService service;

  @GetMapping(path = "/optimize/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get all optimizations by projectId")
  public List<OptimizationResponse> getAllOptimizationByProject(
      @PathVariable final String projectId) {
    return service.getAllOptimizationByProject(projectId).stream()
        .map(OptimizationResponse::new)
        .collect(Collectors.toList());
  }

  @PostMapping(path = "/optimize/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Trigger an optimization on a projectId")
  // TODO: When (if) next steps are followable somewhere, the response should include a link or
  // something
  //  to where the user can follow the steps
  public OptimizationResponse optimizeProject(@PathVariable("projectId") String projectId) {
    Optimization optimization = service.createNewOptimization(projectId);
    service.optimizeProject(optimization);
    return new OptimizationResponse(optimization);
  }

  @GetMapping(
      path = "/optimize/{projectId}/{optimizationId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a single optimization from a project and dataset with its results")
  public OptimizationResultsResponse getOptimizationResults(
      @PathVariable final String projectId, @PathVariable final Long optimizationId) {
    Optimization optimization = service.getOptimization(projectId, optimizationId);
    List<OptimizationResult> optimizationResults =
        service.getOptimizationResults(projectId, optimizationId);
    return new OptimizationResultsResponse(optimization, optimizationResults);
  }

  @DeleteMapping(path = "/optimize/{projectId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void deleteAllOptimization(@PathVariable final String projectId) {
    service.destroyAllMaterializedViewsByProject(projectId);
  }
}
