package com.achilio.mvm.service.databases.entities;

public class FetchedMaterializedViewEvent {

  private final String name;
  private final String datasetName;
  private final String tableName;
  private final long createdAt;
  private final String operationType;

  public FetchedMaterializedViewEvent(
      final String name,
      final String datasetName,
      final String tableName,
      final long createdAt,
      final String operationType) {
    this.name = name;
    this.datasetName = datasetName;
    this.tableName = tableName;
    this.createdAt = createdAt;
    this.operationType = operationType;
  }

  public String getName() {
    return name;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public String getTableName() {
    return tableName;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public String getOperationType() {
    return operationType;
  }
}
