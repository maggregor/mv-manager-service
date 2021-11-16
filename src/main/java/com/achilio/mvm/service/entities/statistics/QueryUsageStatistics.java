package com.achilio.mvm.service.entities.statistics;

public class QueryUsageStatistics {

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
}
