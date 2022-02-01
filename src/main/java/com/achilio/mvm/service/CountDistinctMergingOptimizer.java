package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.List;

public class CountDistinctMergingOptimizer implements Optimizer {

  CountDistinctMergingOptimizer() {
  }

  @Override
  public List<FieldSet> optimize(List<FieldSet> fieldSet) {
    // TODO: Implement simple cardinality merge.
    return fieldSet;
  }

}
