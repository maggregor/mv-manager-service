package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.Field;
import com.google.zetasql.SimpleCatalog;

public abstract class ZetaSQLFieldSetExtractVisitor extends FieldSetExtractVisitor {

  public static final String NOT_REGULAR_COLUMN_PREFIX_TO_SKIP = "$";
  private SimpleCatalog catalog;

  public ZetaSQLFieldSetExtractVisitor(SimpleCatalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public boolean filterAllowAddField(Field field) {
    return !field.name().startsWith(NOT_REGULAR_COLUMN_PREFIX_TO_SKIP);
  }

  protected SimpleCatalog getCatalog() {
    return this.catalog;
  }
}
