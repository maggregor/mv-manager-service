package com.achilio.mvm.service.visitors.fields;

import java.util.HashSet;
import java.util.Set;

public enum FieldSetFactory {
  ;

  public final static FieldSet EMPTY_FIELD_SET = createFieldSet(new HashSet<>());

  public static FieldSet createFieldSet(Set<Field> fields) {
    return new DefaultFieldSet(fields);
  }
}
