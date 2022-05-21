package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.QueryPattern;
import java.util.ArrayList;
import java.util.List;

public class DontDoNothingMVGenerator implements MVGenerator {

  DontDoNothingMVGenerator() {
  }

  @Override
  public List<QueryPattern> generate(List<QueryPattern> queryPattern) {
    return new ArrayList<>(queryPattern);
  }
}
