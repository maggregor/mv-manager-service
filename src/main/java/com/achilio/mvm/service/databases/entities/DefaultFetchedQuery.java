package com.achilio.mvm.service.databases.entities;

public class DefaultFetchedQuery implements FetchedQuery {

  private String statement;
  private long cost;
  private String projectId;
  private String datasetName;
  private String tableName;
  private boolean usingManagedMV;

  public DefaultFetchedQuery(final String statement, final long cost) {
    this.statement = statement;
    this.cost = cost;
  }

  public long cost() {
    return cost;
  }

  @Override
  public String getProjectId() {
    return this.projectId;
  }

  @Override
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public String getDatasetName() {
    return this.datasetName;
  }

  @Override
  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  @Override
  public String getTableName() {
    return this.tableName;
  }

  @Override
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public boolean isUsingManagedMV() {
    return this.usingManagedMV;
  }

  @Override
  public void setUsingManagedMV(boolean usingManagedMV) {
    this.usingManagedMV = usingManagedMV;
  }

  public String statement() {
    return statement;
  }
}
