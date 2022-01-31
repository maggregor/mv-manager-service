package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;

public class OptimizationResponse {

  private Long id;
  private String projectId;

  public OptimizationResponse(Optimization optimization) {
    this.id = optimization.getId();
    this.projectId = optimization.getProjectId();
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
}
