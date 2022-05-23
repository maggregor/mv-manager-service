package com.achilio.mvm.service.visitors.querypattern;

import com.achilio.mvm.service.entities.QueryPattern;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.Visitor;
import java.util.ArrayList;
import java.util.List;

public class ZetaSQLQueryPatternExtractEntryPointVisitor extends Visitor {

  private final SimpleCatalog catalog;
  private final List<QueryPattern> allQueryPatterns = new ArrayList<>();
  private final String defaultProjectId;

  public ZetaSQLQueryPatternExtractEntryPointVisitor(
      String defaultProjectId, SimpleCatalog catalog) {
    this.defaultProjectId = defaultProjectId;
    this.catalog = catalog;
  }

  @Override
  public void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  @Override
  public void visit(ResolvedNodes.ResolvedQueryStmt node) {
    ZetaSQLQueryPatternExtractStatementVisitor queryVisitor =
        new ZetaSQLQueryPatternExtractStatementVisitor(defaultProjectId, catalog);
    node.accept(queryVisitor);
    this.allQueryPatterns.addAll(queryVisitor.getAllQueryPatterns());
  }

  public List<QueryPattern> getAllQueryPatterns() {
    return this.allQueryPatterns;
  }
}
