package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.zetasql.Analyzer;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedColumn;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedAggregateFunctionCall;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedColumnRef;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedComputedColumn;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedExpr;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFilterScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFunctionCallBase;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedOutputColumn;
import java.util.ArrayList;
import java.util.List;

public class ZetaSQLFieldSetExtractGlobalVisitor extends ZetaSQLFieldSetExtractVisitor {

  private final static String NOT_REGULAR_TABLE_PREFIX = "$";

  // Useful to build SQL expression from ResolvedNode.
  private final SimpleCatalog catalog;

  ZetaSQLFieldSetExtractGlobalVisitor(SimpleCatalog catalog) {
    this.catalog = catalog;
  }

  /**
   * Visit the output columns of the request.
   *
   * @param node
   */
  @Override
  public void visit(ResolvedOutputColumn node) {
    addReference(node.getColumn());
  }

  /**
   * Visit the filter clause and visit nodes in expression. ie: {@code WHERE col = 'token'}
   *
   * @param node
   */
  @Override
  public void visit(ResolvedFilterScan node) {
    FieldSetExtractVisitor visitor = new ZetaSQLFieldSetExtractFilterVisitor();
    node.getFilterExpr().accept(visitor);
    this.merge(visitor.fieldSet());
    super.visit(node);
  }

  /**
   * Visit the computed columns. A computed columns may be an alias on function or on ref column.
   */
  @Override
  public void visit(ResolvedComputedColumn node) {
    final ResolvedExpr expr = node.getExpr();
    if (expr instanceof ResolvedColumnRef) {
      addReference(expr);
    } else if (expr instanceof ResolvedAggregateFunctionCall) {
      addAggregate(expr);
    } else if (expr instanceof ResolvedFunctionCallBase) {
      addFunction(expr);
    }
    super.visit(node);
  }

  /**
   * Add a ResolvedColumn as ReferenceField. Checks if the ResolvedColumn isn't an alias.
   */
  private void addReference(ResolvedColumn column) {
    Preconditions.checkNotNull(column, "Reference is null.");
    if (isColumnFromRegularTable(column)) {
      // It's reference to a table of the catalog.
      addField(new ReferenceField(column.getName()));
    }
  }

  /**
   * Add ResolvedExpr as reference Simple cast method.
   */
  private void addReference(ResolvedExpr expr) {
    final ResolvedColumnRef ref = (ResolvedColumnRef) expr;
    final ResolvedColumn column = ref.getColumn();
    addReference(column);
  }

  private void addAggregate(ResolvedExpr expr) {
    String sql = buildSQLFunction((ResolvedAggregateFunctionCall) expr);
    this.addField(new AggregateField(sql));
  }

  private void addFunction(ResolvedExpr expr) {
    String sql = buildSQLFunction((ResolvedFunctionCallBase) expr);
    this.addField(new FunctionField(sql));
  }

  private String buildSQLFunction(ResolvedFunctionCallBase func) {
    String expression = Analyzer.buildExpression(func, catalog);
    return hackMappingColumnsInFunction(expression, func);
  }

  /**
   * Checks if the ResolvedColumn come from a table and is not an alias.
   *
   * @param column
   * @return
   */
  private boolean isColumnFromRegularTable(final ResolvedColumn column) {
    Preconditions.checkNotNull(column, "ResolvedColumn is null");
    final String tableName = column.getTableName();
    return !tableName.startsWith(NOT_REGULAR_TABLE_PREFIX);
  }

  private String hackMappingColumnsInFunction(String expression, ResolvedFunctionCallBase expr) {
    List<ResolvedColumnRef> refs = new ArrayList<>();
    hackFindColumnsRefInNode(expr.getArgumentList(), refs);
    int count = 0;
    for (ResolvedColumnRef ref : refs) {
      // Hack in order to build the node with source columns name.
      expression = expression.replaceAll("a_" + ++count, ref.getColumn().getName());
    }
    return expression;
  }

  private void hackFindColumnsRefInNode(ImmutableList<ResolvedExpr> exprs,
      List<ResolvedColumnRef> refs) {
    for (ResolvedExpr expr : exprs) {
      if (expr instanceof ResolvedFunctionCallBase) {
        hackFindColumnsRefInNode(((ResolvedFunctionCallBase) expr).getArgumentList(), refs);
      } else if (expr instanceof ResolvedColumnRef) {
        refs.add((ResolvedColumnRef) expr);
      }
    }
  }
}
