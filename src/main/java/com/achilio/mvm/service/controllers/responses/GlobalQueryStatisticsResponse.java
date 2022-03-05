package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class GlobalQueryStatisticsResponse {

  @JsonProperty("global")
  private QueryStatistics global;

  @JsonProperty("details")
  private Map<Scope, QueryStatistics> details;

  public GlobalQueryStatisticsResponse(GlobalQueryStatistics statistics) {
    this.global = statistics.getTotalStatistics();
    this.details = statistics.getDetails();
  }
}
