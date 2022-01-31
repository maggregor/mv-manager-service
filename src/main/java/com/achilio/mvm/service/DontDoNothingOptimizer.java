package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DontDoNothingOptimizer implements Optimizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DontDoNothingOptimizer.class);

  private final int maxFieldSet;

  DontDoNothingOptimizer(int maxFieldSet) {
    this.maxFieldSet = maxFieldSet;
  }

  @Override
  public Set<FieldSet> optimize(Set<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    return fieldSet.stream().limit(maxFieldSet).collect(Collectors.toSet());
  }

}