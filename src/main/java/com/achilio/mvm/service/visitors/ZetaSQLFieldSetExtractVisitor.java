package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.Field;

public class ZetaSQLFieldSetExtractVisitor extends FieldSetExtractVisitor {

  private static final String NOT_REGULAR_COLUMN_PREFIX_TO_SKIP = "$";

  @Override
  public boolean filterAllowAddField(Field field) {
    return !field.name().startsWith(NOT_REGULAR_COLUMN_PREFIX_TO_SKIP);
  }
}
