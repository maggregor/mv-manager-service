package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DontDoNothingOptimizer implements Optimizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DontDoNothingOptimizer.class);

  DontDoNothingOptimizer() {}

  @Override
  public List<FieldSet> optimize(List<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    return new ArrayList<>(fieldSet);
  }
}
