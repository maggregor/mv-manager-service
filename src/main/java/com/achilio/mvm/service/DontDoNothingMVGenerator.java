package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.ArrayList;
import java.util.List;

public class DontDoNothingMVGenerator implements MVGenerator {

  DontDoNothingMVGenerator() {}

  @Override
  public List<FieldSet> generate(List<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    return new ArrayList<>(fieldSet);
  }
}
