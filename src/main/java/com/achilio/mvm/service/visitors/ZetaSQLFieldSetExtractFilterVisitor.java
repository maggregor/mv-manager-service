package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.google.zetasql.resolvedast.ResolvedNodes;

public class ZetaSQLFieldSetExtractFilterVisitor extends ZetaSQLFieldSetExtractVisitor {

  @Override
  public void visit(ResolvedNodes.ResolvedColumnRef node) {
    final String referenceName = node.getColumn().getName();
    this.addField(new ReferenceField(referenceName));
    super.visit(node);
  }
}
