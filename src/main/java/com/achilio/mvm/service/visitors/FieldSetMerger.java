package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Merge FieldSet statistics. */
public class FieldSetMerger {

  /**
   * Merge QueryUsageStatistics
   *
   * @param fieldSets - Input FieldSet
   * @return merged fieldset
   */
  public static List<FieldSet> mergeSame(List<FieldSet> fieldSets) {
    Map<Integer, FieldSet> merged = new HashMap<>();
    for (FieldSet fieldSet : fieldSets) {
      int key = fieldSet.hashCode();
      if (merged.containsKey(key)) {
        merged.get(key).merge(fieldSet);
      } else {
        merged.put(key, fieldSet);
      }
    }
    return new ArrayList<>(merged.values());
  }
}
