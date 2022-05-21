package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNodes;

/**
 * Where clause
 */
public class ZetaSQLFieldSetExtractFilterExprVisitor extends ZetaSQLFieldSetExtractVisitor {

  public ZetaSQLFieldSetExtractFilterExprVisitor(SimpleCatalog catalog) {
    super(catalog);
  }

  @Override
  public void visit(ResolvedNodes.ResolvedColumnRef node) {
    final String referenceName = node.getColumn().getName();
    this.addField(new Field(FieldType.REFERENCE, referenceName));
    super.visit(node);
  }
}
