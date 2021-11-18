package com.achilio.mvm.service.entities.statistics;

import java.util.HashMap;
import java.util.Map;

public class GlobalQueryStatistics {

  public static final String SCOPE_IN = "in";
  public static final String SCOPE_OUT = "out";
  public static final String SCOPE_CACHED = "cached";

  private final QueryStatistics total;
  private final Map<String, QueryStatistics> details = new HashMap<>();

  public GlobalQueryStatistics() {
    total = new QueryStatistics();
  }

  public GlobalQueryStatistics(boolean computeIneligibilityReasons) {
    total = new QueryStatistics(computeIneligibilityReasons);
  }

  public void addStatistic(String name, QueryStatistics statistics) {
    // Global metrics
    total.addQueries(statistics.getTotalQueries());
    total.addProcessedBytes(statistics.getTotalProcessedBytes());
    total.addBilledBytes(statistics.getTotalBilledBytes());
    total.addEligible(statistics.getEligible());
    total.addIneligibles(statistics.getIneligible());
    total.addIneligibleReasons(statistics.getIneligibleReasons());
    // Add children statistics
    this.details.put(name, statistics);
  }

  public QueryStatistics getTotalStatistics() {
    return this.total;
  }

  public Map<String, QueryStatistics> getDetails() {
    return this.details;
  }
}
