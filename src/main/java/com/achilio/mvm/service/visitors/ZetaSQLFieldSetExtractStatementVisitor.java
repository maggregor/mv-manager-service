package com.achilio.mvm.service.visitors;

import static com.achilio.mvm.service.visitors.FieldSetIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN;
import static com.achilio.mvm.service.visitors.FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.entities.TableRef;
import com.achilio.mvm.service.entities.TableRef.TableRefType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.zetasql.Analyzer;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedColumn;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedAggregateFunctionCall;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedColumnRef;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedComputedColumn;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedExpr;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFilterScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedFunctionCallBase;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedJoinScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedOutputColumn;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedScan;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedTableScan;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZetaSQLFieldSetExtractStatementVisitor extends ZetaSQLFieldSetExtractVisitor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ZetaSQLFieldSetExtractStatementVisitor.class);
  private static final String NOT_REGULAR_TABLE_PREFIX = "$";

  // Useful to build SQL expression from ResolvedNode.
  private final List<QueryPattern> subQueryPatterns = new ArrayList<>();
  private final String defaultProjectId;

  public ZetaSQLFieldSetExtractStatementVisitor(String defaultProjectId,
      SimpleCatalog catalog) {
    super(catalog);
    this.defaultProjectId = defaultProjectId;
  }

  @Override
  public void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  /**
   * Visit the output columns of the request.
   *
   * @param node
   */
  @Override
  public void visit(ResolvedOutputColumn node) {
    if (isColumnFromRegularTable(node.getColumn())) {
      this.getQueryPattern().addIneligibilityReason(DOES_NOT_CONTAIN_A_GROUP_BY);
    }
    addReference(node.getColumn());
  }

  @Override
  public void visit(ResolvedTableScan node) {
    TableRef referenceTableId = this.getQueryPattern().getMainTable();
    if (referenceTableId == null) {
      String currentTableName = node.getTable().getName();
      ATableId currentTableId = getTable(node);
      if (currentTableId == null) {
        throw new IllegalArgumentException(
            "Can't find TableId on ResolvedTableScan: " + currentTableName);
      }
      this.getQueryPattern().addTableRef(new TableRef(currentTableId, TableRefType.MAIN));
    }
  }

  @Override
  public void visit(ResolvedJoinScan node) {
    Optional<ATableId> tableIdLeft = findTableInResolvedScan(node.getLeftScan());
    if (tableIdLeft.isPresent() && this.getQueryPattern().getMainTable() == null) {
      this.setTableReference(tableIdLeft.get());
    }
    TableRefType type = TableRefType.valueOf(node.getJoinType().name());
    Optional<ATableId> tableIdRight = findTableInResolvedScan(node.getRightScan());
    tableIdRight.ifPresent(t -> addTableJoin(t, type));
    if (/*!type.equals(JoinType.INNER)*/ true) {
      // INNER JOINS are no longer supported until the fix on old school inner join syntax.
      this.getQueryPattern().addIneligibilityReason(CONTAINS_UNSUPPORTED_JOIN);
    }
    super.defaultVisit(node);
  }

  public Optional<ATableId> findTableInResolvedScan(ResolvedScan scan) {
    if (scan instanceof ResolvedTableScan) {
      return Optional.ofNullable(getTable((ResolvedTableScan) scan));
    }

    return Optional.empty();
  }

  public ATableId getTable(ResolvedTableScan scan) {
    ATableId tableId = ATableId.parse(scan.getTable().getName());
    if (tableId == null) {
      return null;
    } else if (tableId.getProjectId() == null) {
      tableId = ATableId.of(defaultProjectId, tableId.getDatasetName(), tableId.getTableName());
    }
    return tableId;
  }

  /**
   * Visit the filter clause and visit nodes in expression. ie: {@code WHERE col = 'token'}
   *
   * @param node
   */
  @Override
  public void visit(ResolvedFilterScan node) {
    FieldSetExtractVisitor visitor = new ZetaSQLFieldSetExtractFilterExprVisitor(
        getCatalog());
    node.getFilterExpr().accept(visitor);
    this.getQueryPattern().merge(visitor.getQueryPattern());
    super.visit(node);
  }

  /**
   * Visit the computed columns. A computed columns may be an alias on function or on ref column.
   */
  @Override
  public void visit(ResolvedComputedColumn node) {
    if (node.getColumn().getTableName().startsWith("$groupby")) {
      this.getQueryPattern().removeIneligibilityReason(DOES_NOT_CONTAIN_A_GROUP_BY);
    }
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
   * SubQuery in expression (NOT in FROM): creates a new QueryPattern SELECT a as (SELECT sum() FROM
   * mytable) FROM mytable
   *
   * @param node
   */
  @Override
  public void visit(ResolvedNodes.ResolvedSubqueryExpr node) {
    this.extractNewQueryPattern(node.getSubquery());
  }

  /**
   * WITH subQuery: creates a new QueryPattern
   *
   * @param node
   */
  @Override
  public void visit(ResolvedNodes.ResolvedWithEntry node) {
    this.extractNewQueryPattern(node.getWithSubquery());
  }

  /** - ResolvedWithEntry */

  /**
   * ResolvedQueryStmt
   *
   * @param node
   */
  private void extractNewQueryPattern(ResolvedNodes.ResolvedScan node) {
    //
    ZetaSQLFieldSetExtractStatementVisitor visitor =
        new ZetaSQLFieldSetExtractStatementVisitor(defaultProjectId, getCatalog());
    node.accept(visitor);
    subQueryPatterns.addAll(visitor.getAllQueryPatterns());
  }

  /**
   * Add a ResolvedColumn as ReferenceField. Checks if the ResolvedColumn isn't an alias.
   */
  private void addReference(ResolvedColumn column) {
    Preconditions.checkNotNull(column, "Reference is null.");
    if (isColumnFromRegularTable(column)) {
      // It's reference to a table of the catalog.
      addField(new Field(FieldType.REFERENCE, column.getName()));
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
    addField(new Field(FieldType.AGGREGATE, sql));
  }

  private void addFunction(ResolvedExpr expr) {
    String sql = buildSQLFunction((ResolvedFunctionCallBase) expr);
    addField(new Field(FieldType.FUNCTION, sql));
  }

  /**
   * Returns all QueryPattern discovered in the statement - Remove empty QueryPattern - Remove
   * QueryPattern without reference table
   *
   * @return
   */
  public List<QueryPattern> getAllQueryPatterns() {
    List<QueryPattern> QueryPatterns = new ArrayList<>();
    // Not empty field set
    QueryPatterns.add(super.getQueryPattern());
    QueryPatterns.addAll(this.subQueryPatterns);
    return QueryPatterns.stream()
        .filter(f -> !f.isEmpty() && f.getMainTable() != null)
        .collect(Collectors.toList());
  }

  private String buildSQLFunction(ResolvedFunctionCallBase func) {
    String expression = Analyzer.buildExpression(func, getCatalog());
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

  private void hackFindColumnsRefInNode(
      ImmutableList<ResolvedExpr> exprs, List<ResolvedColumnRef> refs) {
    for (ResolvedExpr expr : exprs) {
      if (expr instanceof ResolvedFunctionCallBase) {
        hackFindColumnsRefInNode(((ResolvedFunctionCallBase) expr).getArgumentList(), refs);
      } else if (expr instanceof ResolvedColumnRef) {
        refs.add((ResolvedColumnRef) expr);
      }
    }
  }
}
