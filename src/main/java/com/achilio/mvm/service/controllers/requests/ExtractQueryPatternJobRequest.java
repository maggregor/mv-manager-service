package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExtractQueryPatternJobRequest {

  @JsonProperty
  private String projectId;
}
