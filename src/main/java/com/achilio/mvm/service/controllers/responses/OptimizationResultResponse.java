package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.OptimizationResult;

public class OptimizationResultResponse {

  private Long id;
  private String projectId;
  private String tableName;
  private String statement;
  private long processedBytes;
  private int queries;

  public OptimizationResultResponse(OptimizationResult optimizationResult) {
    this.id = optimizationResult.getId();
    this.projectId = optimizationResult.getProjectId();
    this.tableName = optimizationResult.getTableName();
    this.statement = optimizationResult.getStatement();
    this.processedBytes = optimizationResult.getTotalProcessedBytes();
    this.queries = optimizationResult.getQueries();
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

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

  public int getQueries() {
    return this.queries;
  }

  public long getProcessedBytes() {
    return this.processedBytes;
  }
}
