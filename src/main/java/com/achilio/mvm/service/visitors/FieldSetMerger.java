package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merge FieldSet statistics.
 */
public class FieldSetMerger {

  /**
   * Merge QueryUsageStatistics
   * <p>
   * ie: Field1, Field2, 100Mb scanned Field1, Field2, 100Mb scanned Field3, 50Mb scanned
   * </p>
   * <p>
   * output: Field1, Field2, 200Mb scanned Field3, 50Mb scanned
   * </p>
   *
   * @param fieldSets - Input FieldSet
   * @return merged fieldset
   */
  public static List<FieldSet> merge(List<FieldSet> fieldSets) {
    Map<FieldSet, QueryUsageStatistics> merged = new HashMap<>();
    for (FieldSet fieldSet : fieldSets) {
      if (!merged.containsKey(fieldSet)) {
        merged.put(fieldSet, fieldSet.getStatistics());
      } else {
        merged.get(fieldSet).addQueryUsageStatistics(fieldSet.getStatistics());
      }
    }
    return merged.entrySet().stream().map(entry -> {
      entry.getKey().setStatistics(entry.getValue());
      return entry.getKey();
    }).collect(Collectors.toList());
  }
}