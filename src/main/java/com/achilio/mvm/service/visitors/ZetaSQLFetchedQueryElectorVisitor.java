package com.achilio.mvm.service.visitors;

import static com.achilio.mvm.service.visitors.QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedAggregateScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFilterScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedQueryStmt;

public class ZetaSQLFetchedQueryElectorVisitor extends ResolvedNodes.Visitor {

  private final FetchedQuery query;

  public ZetaSQLFetchedQueryElectorVisitor(FetchedQuery query) {
    this.query = query;
  }

  @Override
  protected void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  @Override
  public void visit(ResolvedQueryStmt node) {
    query.addQueryIneligibilityReason(DOES_NOT_FILTER_OR_AGGREGATE);
    super.visit(node);
  }

  @Override
  public void visit(ResolvedAggregateScan node) {
    query.removeQueryIneligibilityReason(DOES_NOT_FILTER_OR_AGGREGATE);
    super.visit(node);
  }

  @Override
  public void visit(ResolvedFilterScan node) {
    query.removeQueryIneligibilityReason(DOES_NOT_FILTER_OR_AGGREGATE);
    super.visit(node);
  }
}
