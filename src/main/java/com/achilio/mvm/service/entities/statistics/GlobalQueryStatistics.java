package com.achilio.mvm.service.entities.statistics;

import java.util.HashMap;
import java.util.Map;

public class GlobalQueryStatistics {

  private final QueryStatistics total;
  private final Map<Scope, QueryStatistics> details = new HashMap<>();

  public GlobalQueryStatistics() {
    total = new QueryStatistics();
    for (Scope scope : Scope.values()) {
      details.put(scope, new QueryStatistics());
    }
  }

  public void addStatistic(Scope scope, QueryStatistics statistics) {
    // Global metrics
    total.addQueries(statistics.getTotalQueries());
    total.addProcessedBytes(statistics.getTotalProcessedBytes());
    total.addBilledBytes(statistics.getTotalBilledBytes());
    // Add children statistics
    this.details.put(scope, statistics);
  }

  public QueryStatistics getTotalStatistics() {
    return this.total;
  }

  public Map<Scope, QueryStatistics> getDetails() {
    return this.details;
  }

  public enum Scope {
    IN,
    OUT,
    CACHED;

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }
}
