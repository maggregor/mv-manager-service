package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

public class OptimizationResultsResponse {

  @JsonUnwrapped private final OptimizationResponse optimization;
  private final List<OptimizationResult> results;

  public OptimizationResultsResponse(
      Optimization optimization, List<OptimizationResult> optimizationResults) {
    this.optimization = new OptimizationResponse(optimization);
    this.results = optimizationResults;
  }

  public OptimizationResponse getOptimization() {
    return optimization;
  }

  public List<OptimizationResult> getResults() {
    return results;
  }
}
