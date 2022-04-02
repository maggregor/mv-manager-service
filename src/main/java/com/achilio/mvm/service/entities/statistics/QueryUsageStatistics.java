package com.achilio.mvm.service.entities.statistics;

public class QueryUsageStatistics {

  private int queries;
  private long billedBytes;
  private long processedBytes;

  public QueryUsageStatistics() {
    this(1, 0, 0);
  }

  public QueryUsageStatistics(int queries, long billedBytes, long processedBytes) {
    this.queries = queries;
    this.billedBytes = billedBytes;
    this.processedBytes = processedBytes;
  }

  public long getBilledBytes() {
    return billedBytes;
  }

  public void setBilledBytes(long billedBytes) {
    this.billedBytes = billedBytes;
  }

  public long getProcessedBytes() {
    return processedBytes;
  }

  public void setProcessedBytes(long processedBytes) {
    this.processedBytes = processedBytes;
  }

  public int getQueryCount() {
    return this.queries;
  }

  public void addQueryUsageStatistics(QueryUsageStatistics statistics) {
    this.billedBytes += statistics.getBilledBytes();
    this.processedBytes += statistics.getProcessedBytes();
    this.queries++;
  }
}
