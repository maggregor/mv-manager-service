package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import java.util.List;

public class OptimizationResultsResponse {

  private Optimization optimization;
  private List<OptimizationResult> results;

  public OptimizationResultsResponse(
      Optimization optimization, List<OptimizationResult> optimizationResults) {
    this.optimization = optimization;
    this.results = optimizationResults;
  }

  public Optimization getOptimization() {
    return optimization;
  }

  public List<OptimizationResult> getResults() {
    return results;
  }
}
