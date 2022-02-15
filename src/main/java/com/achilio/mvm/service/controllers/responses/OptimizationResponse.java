package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import java.util.Date;

public class OptimizationResponse {

  private Long id;
  private Date createdDate;
  private String projectId;
  private String datasetName;
  private Double eligiblePercent;
  private Integer mvMaxPlan;
  private Integer mvAppliedCount;
  private Integer mvProposalCount;
  private Integer mvMaxPerTable;

  public OptimizationResponse(Optimization optimization) {
    this.id = optimization.getId();
    this.createdDate = optimization.getCreatedDate();
    this.projectId = optimization.getProjectId();
    this.eligiblePercent = optimization.getQueryEligiblePercentage();
    this.mvMaxPlan = optimization.getMvMaxPlan();
    this.mvAppliedCount = optimization.getMvAppliedCount();
    this.mvProposalCount = optimization.getMvProposalCount();
    this.mvMaxPerTable = optimization.getMvMaxPerTable();
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

  public Double getEligiblePercent() {
    return this.eligiblePercent;
  }

  public void setEligiblePercent(Double eligiblePercent) {
    this.eligiblePercent = eligiblePercent;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Integer getMvMaxPlan() {
    return mvMaxPlan;
  }

  public void setMvMaxPlan(Integer mvMaxPlan) {
    this.mvMaxPlan = mvMaxPlan;
  }

  public Integer getMvAppliedCount() {
    return mvAppliedCount;
  }

  public void setMvAppliedCount(Integer mvAppliedCount) {
    this.mvAppliedCount = mvAppliedCount;
  }

  public Integer getMvProposalCount() {
    return mvProposalCount;
  }

  public void setMvProposalCount(Integer mvProposalCount) {
    this.mvProposalCount = mvProposalCount;
  }

  public Integer getMvMaxPerTable() {
    return mvMaxPerTable;
  }

  public void setMvMaxPerTable(Integer mvMaxPerTable) {
    this.mvMaxPerTable = mvMaxPerTable;
  }
}
