package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.Optimization;

public class OptimizationResponse {

  private Long id;
  private String projectId;
  private String datasetName;
  private String regionId;

  public OptimizationResponse(Optimization optimization) {
    this.id = optimization.getId();
    this.projectId = optimization.getProjectId();
    this.datasetName = optimization.getDatasetName();
    this.regionId = optimization.getRegionId();
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

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public String getRegionId() {
    return regionId;
  }

  public void setRegionId(String regionId) {
    this.regionId = regionId;
  }
}
