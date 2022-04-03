package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.ArrayList;
import java.util.List;

public class DontDoNothingOptimizer implements Optimizer {

  DontDoNothingOptimizer() {}

  @Override
  public List<FieldSet> optimize(List<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    return new ArrayList<>(fieldSet);
  }
}
