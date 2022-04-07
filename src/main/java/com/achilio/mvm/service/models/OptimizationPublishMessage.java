package com.achilio.mvm.service.models;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptimizationPublishMessage {

  private String serviceAccount;
  private List<Map<String, String>> optimizationResults;
}
