package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.QueryPattern;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.Visitor;
import java.util.ArrayList;
import java.util.List;

public class ZetaSQLFieldSetExtractEntryPointVisitor extends Visitor {

  private final SimpleCatalog catalog;
  private final List<QueryPattern> allQueryPatterns = new ArrayList<>();
  private final String defaultProjectId;

  public ZetaSQLFieldSetExtractEntryPointVisitor(String defaultProjectId, SimpleCatalog catalog) {
    this.defaultProjectId = defaultProjectId;
    this.catalog = catalog;
  }

  @Override
  public void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  @Override
  public void visit(ResolvedNodes.ResolvedQueryStmt node) {
    ZetaSQLFieldSetExtractStatementVisitor queryVisitor =
        new ZetaSQLFieldSetExtractStatementVisitor(defaultProjectId, catalog);
    node.accept(queryVisitor);
    this.allQueryPatterns.addAll(queryVisitor.getAllQueryPatterns());
  }

  public List<QueryPattern> getAllQueryPatterns() {
    return this.allQueryPatterns;
  }
}
