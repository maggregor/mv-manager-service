package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Set;

public class CountDistinctMergingOptimizer implements Optimizer {

  CountDistinctMergingOptimizer() {
  }

  @Override
  public Set<FieldSet> optimize(Set<FieldSet> fieldSet) {
    // TODO: Implement simple cardinality merge.
    return fieldSet;
  }

}
