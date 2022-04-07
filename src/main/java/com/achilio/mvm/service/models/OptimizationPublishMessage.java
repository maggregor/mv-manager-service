package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptimizationPublishMessage {

  @JsonProperty("serviceAccount")
  private String serviceAccount;

  @JsonProperty("queries")
  private List<Map<String, String>> optimizationResults;
}
