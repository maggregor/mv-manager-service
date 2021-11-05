package com.achilio.mvm.service.databases.entities;

public class DefaultFetchedQuery implements FetchedQuery {

  private String statement;
  private long billedBytes;
  private long processedBytes;
  private String projectId;
  private String datasetName;
  private String tableName;
  private boolean usingManagedMV;
  private boolean cached;

  public DefaultFetchedQuery(final String statement, final long billedBytes) {
    this.statement = statement;
    this.billedBytes = billedBytes;
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

  @Override
  public long getBilledBytes() {
    return this.billedBytes;
  }

  @Override
  public void setBilledBytes(long billedBytes) {
    this.billedBytes = billedBytes;
  }

  @Override
  public long getProcessedBytes() {
    return this.processedBytes;
  }

  @Override
  public void setProcessedBytes(long processedBytes) {
    this.processedBytes = processedBytes;
  }

  @Override
  public boolean isCached() {
    return this.cached;
  }

  @Override
  public void setUseCache(boolean useCache) {
    this.cached = useCache;
  }
}
