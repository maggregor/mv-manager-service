package com.achilio.mvm.service.entities.statistics;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import java.util.List;

public class QueryStatistics {

  private int totalQueries = 0;
  private long totalBilledBytes = 0;
  private long totalProcessedBytes = 0;

  public QueryStatistics() {}

  public QueryStatistics(List<FetchedQuery> queries) {
    queries.forEach(this::addQuery);
  }

  private void addQuery(FetchedQuery query) {
    incrementQueries();
    addBilledBytes(query.getStatistics().getBilledBytes());
    addProcessedBytes(query.getStatistics().getProcessedBytes());
  }

  public int getTotalQueries() {
    return totalQueries;
  }

  public long getTotalBilledBytes() {
    return totalBilledBytes;
  }

  public long getTotalProcessedBytes() {
    return totalProcessedBytes;
  }

  public void incrementQueries() {
    this.totalQueries += 1;
  }

  public void addProcessedBytes(long processedBytes) {
    this.totalProcessedBytes += processedBytes;
  }

  public void addBilledBytes(long billedBytes) {
    this.totalBilledBytes += billedBytes;
  }

  public void addQueries(int queries) {
    this.totalQueries += queries;
  }
}
