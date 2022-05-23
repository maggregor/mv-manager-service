package com.achilio.mvm.service.visitors.querypattern;

import com.achilio.mvm.service.entities.QueryPattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Merge FieldSet statistics. */
public enum QueryPatternMerger {
  ;

  /**
   * Merge QueryUsageStatistics
   *
   * @return merged fieldset
   */
  public static List<QueryPattern> mergeSame(List<QueryPattern> queryPatterns) {
    Map<Integer, QueryPattern> merged = new HashMap<>();
    for (QueryPattern queryPattern : queryPatterns) {
      int key = queryPattern.hashCode();
      if (merged.containsKey(key)) {
        merged.get(key).merge(queryPattern);
      } else {
        merged.put(key, queryPattern);
      }
    }
    return new ArrayList<>(merged.values());
  }
}
