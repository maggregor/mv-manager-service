package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetcherQueryJobRequest {

  @JsonProperty private Integer timeframe;

  public FetcherQueryJobRequest(Integer timeframe) {
    this.timeframe = timeframe;
  }

  public Integer getTimeframe() {
    return timeframe;
  }
}
