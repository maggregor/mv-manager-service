package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Optimization;
import java.util.Date;

public class OptimizationResponse {

  private final Long id;
  private final Date createdDate;
  private final String projectId;
  private final Double eligiblePercent;
  private final Integer mvAppliedCount;
  private final Integer mvProposalCount;
  private final Integer mvMaxPerTable;
  private final String username;
  private final String status;

  public OptimizationResponse(Optimization optimization) {
    this.id = optimization.getId();
    this.createdDate = optimization.getCreatedDate();
    this.projectId = optimization.getProjectId();
    this.eligiblePercent = optimization.getQueryEligiblePercentage();
    this.mvAppliedCount = optimization.getMvAppliedCount();
    this.mvProposalCount = optimization.getMvProposalCount();
    this.mvMaxPerTable = optimization.getMvMaxPerTable();
    this.username = optimization.getUsername();
    this.status = optimization.getStatus().description();
  }

  public Long getId() {
    return id;
  }

  public String getProjectId() {
    return projectId;
  }

  public Double getEligiblePercent() {
    return this.eligiblePercent;
  }

  public Integer getMvAppliedCount() {
    return mvAppliedCount;
  }

  public Integer getMvProposalCount() {
    return mvProposalCount;
  }

  public Integer getMvMaxPerTable() {
    return mvMaxPerTable;
  }

  public String getUsername() {
    return username;
  }

  public String getStatus() {
    return this.status;
  }

  public Date getCreatedDate() {
    return this.createdDate;
  }
}
