package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.google.zetasql.resolvedast.ResolvedJoinScanEnums;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedAggregateScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFilterScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedJoinScan;

public class ZetaSQLFieldSetElectorVisitor extends ResolvedNodes.Visitor {

  private final FieldSet fieldSet;

  public ZetaSQLFieldSetElectorVisitor(FieldSet fieldSet) {
    this.fieldSet = fieldSet;
    fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_FILTER_OR_GROUP);
  }

  @Override
  protected void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  @Override
  public void visit(ResolvedAggregateScan node) {
    fieldSet.removeIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_FILTER_OR_GROUP);
    super.visit(node);
  }

  @Override
  public void visit(ResolvedFilterScan node) {
    fieldSet.removeIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_FILTER_OR_GROUP);
    super.visit(node);
  }

  @Override
  public void visit(ResolvedJoinScan node) {
    if (!node.getJoinType().equals(ResolvedJoinScanEnums.JoinType.INNER)) {
      fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN);
    }
  }
}
