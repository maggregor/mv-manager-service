package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import java.util.List;

public class OptimizationResultsResponse {

  private Long id;
  private String projectId;
  private List<OptimizationResult> results;

  public OptimizationResultsResponse(
      Optimization optimization, List<OptimizationResult> optimizationResults) {
    this.id = optimization.getId();
    this.projectId = optimization.getProjectId();
    this.results = optimizationResults;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public List<OptimizationResult> getResults() {
    return results;
  }

  public void setResults(List<OptimizationResult> optimizationResults) {
    this.results = optimizationResults;
  }
}
