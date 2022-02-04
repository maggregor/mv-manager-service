package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortByQueryCostOptimizer implements Optimizer {

  private final int maxFieldSet;

  SortByQueryCostOptimizer(int maxFieldSet) {
    this.maxFieldSet = maxFieldSet;
  }

  @Override
  public List<FieldSet> optimize(List<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    return fieldSet.stream()
        .sorted(Comparator.comparingLong(FieldSet::cost).reversed())
        .limit(maxFieldSet)
        .collect(Collectors.toList());
  }

}
