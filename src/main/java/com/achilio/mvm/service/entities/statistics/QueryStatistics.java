package com.achilio.mvm.service.entities.statistics;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import java.util.List;

public class QueryStatistics {

  private int queries = 0;
  private long billedBytes = 0;
  private long processedBytes = 0;

  public QueryStatistics() {
  }

  public QueryStatistics(List<FetchedQuery> queries) {
    queries.forEach(this::addQuery);
  }

  private void addQuery(FetchedQuery query) {
    incrementQueries();
    addBilledBytes(query.getStatistics().getBilledBytes());
    addProcessedBytes(query.getStatistics().getProcessedBytes());
  }

  private void addStatistics(QueryStatistics statistics) {
    addQueries(statistics.getQueries());
    addBilledBytes(statistics.getBilledBytes());
    addProcessedBytes(statistics.getProcessedBytes());
  }

  public int getQueries() {
    return queries;
  }

  public long getBilledBytes() {
    return billedBytes;
  }

  public long getProcessedBytes() {
    return processedBytes;
  }

  public void incrementQueries() {
    this.queries += 1;
  }

  public void addProcessedBytes(long processedBytes) {
    this.processedBytes += processedBytes;
  }

  public void addBilledBytes(long billedBytes) {
    this.billedBytes += billedBytes;
  }

  public void addQueries(int queries) {
    this.queries += queries;
  }
}
