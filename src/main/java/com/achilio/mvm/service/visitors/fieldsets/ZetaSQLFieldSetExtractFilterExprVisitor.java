package com.achilio.mvm.service.visitors.fieldsets;

import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNodes;

public class ZetaSQLFieldSetExtractFilterExprVisitor extends ZetaSQLFieldSetExtractVisitor {

  public ZetaSQLFieldSetExtractFilterExprVisitor(SimpleCatalog catalog) {
    super(catalog);
  }

  @Override
  public void visit(ResolvedNodes.ResolvedColumnRef node) {
    final String referenceName = node.getColumn().getName();
    this.addField(new ReferenceField(referenceName));
    super.visit(node);
  }
}
