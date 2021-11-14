package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFilterScan;

public class ZetaSQLFetchedQueryElectorVisitor extends ResolvedNodes.Visitor {

  private final FetchedQuery query;

  public ZetaSQLFetchedQueryElectorVisitor(FetchedQuery query) {
    this.query = query;
    addDefaultNotEligibleReasons();
  }

  /**
   * At this step we don't know if the query includes the minimum required to be catch in a MV on
   * BigQuery. At first, we assume that the query is not eligible. Depending on what we find about
   * this, we will remove theses basic reasons of ineligibility.
   *
   * @return
   */
  void addDefaultNotEligibleReasons() {
    //query.addQueryIneligibilityReason(MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN);
    //query.addQueryIneligibilityReason(DOES_NOT_FILTER_OR_AGGREGATE);
  }

  @Override
  public void visit(ResolvedFilterScan node) {
    //query.removeQueryIneligibilityReason(DOES_NOT_FILTER_OR_AGGREGATE);
    super.visit(node);
  }

}
