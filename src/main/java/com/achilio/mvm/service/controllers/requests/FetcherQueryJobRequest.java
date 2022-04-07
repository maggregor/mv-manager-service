package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FetcherQueryJobRequest {

  @JsonProperty private String projectId;
  @JsonProperty private Integer timeframe;
}
