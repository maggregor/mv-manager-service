package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** AggregatedStatistics excludes cached scope */
public class AggregatedStatisticsResponse {

  @JsonProperty("totalQueries")
  private int totalQueries;

  @JsonProperty("percentQueriesIn")
  private double percentQueriesIn;

  @JsonProperty("averageScannedBytes")
  private long averageScannedBytesPerQuery;

  public AggregatedStatisticsResponse(GlobalQueryStatistics statistics) {
    Map<Scope, QueryStatistics> details = statistics.getDetails();
    int totalQueries = statistics.getTotalStatistics().getTotalQueries();
    int totalCached = details.get(Scope.CACHED).getTotalQueries();
    int totalIn = details.get(Scope.IN).getTotalQueries();
    long totalScannedBytes = statistics.getTotalStatistics().getTotalProcessedBytes();
    this.totalQueries = Math.max(0, totalQueries - totalCached);
    this.percentQueriesIn =
        this.totalQueries == 0 ? 0 : Math.round(totalIn * 100.0 / this.totalQueries);
    this.averageScannedBytesPerQuery =
        this.totalQueries == 0 ? 0 : totalScannedBytes / this.totalQueries;
  }

  public int getTotalQueries() {
    return this.totalQueries;
  }

  public double getPercentQueriesIn() {
    return this.percentQueriesIn;
  }

  public long getAverageScannedBytesPerQuery() {
    return this.averageScannedBytesPerQuery;
  }
}
