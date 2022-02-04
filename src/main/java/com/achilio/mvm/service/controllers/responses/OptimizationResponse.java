package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import java.util.Date;

public class OptimizationResponse {

  private Long id;
  private Date createdDate;
  private String projectId;
  private String datasetName;

  public OptimizationResponse(Optimization optimization) {
    this.id = optimization.getId();
    this.createdDate = optimization.getCreatedDate();
    this.projectId = optimization.getProjectId();
    this.datasetName = optimization.getDatasetName();
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

  public Date getCreatedDate() { return createdDate; }

  public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
}
