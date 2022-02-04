package com.achilio.mvm.service.entities.statistics;

public class QueryUsageStatistics {

  private int query = 1;
  private long billedBytes = 0;
  private long processedBytes = 0;

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
    return this.query;
  }

  public void addQueryUsageStatistics(QueryUsageStatistics statistics) {
    this.billedBytes += statistics.getBilledBytes();
    this.processedBytes += statistics.getProcessedBytes();
    this.query++;
  }
}
