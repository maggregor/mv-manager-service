package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FetcherQueryJobRequest {

  @JsonProperty @NotNull private String projectId;
  @JsonProperty @NotNull private Integer timeframe;
}
