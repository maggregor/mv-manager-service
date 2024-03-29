package com.achilio.mvm.service.visitors.querypattern;

import com.achilio.mvm.service.entities.Field;
import com.google.zetasql.SimpleCatalog;

public abstract class ZetaSQLQueryPatternExtractVisitor extends QueryPatternExtractVisitor {

  public static final String NOT_REGULAR_COLUMN_PREFIX_TO_SKIP = "$";
  private final SimpleCatalog catalog;

  public ZetaSQLQueryPatternExtractVisitor(SimpleCatalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public boolean filterAllowAddField(Field field) {
    return !field.getExpression().startsWith(NOT_REGULAR_COLUMN_PREFIX_TO_SKIP);
  }

  protected SimpleCatalog getCatalog() {
    return this.catalog;
  }
}
