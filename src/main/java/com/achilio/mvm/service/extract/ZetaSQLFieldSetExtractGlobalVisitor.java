package com.achilio.mvm.service.extract;

import com.achilio.mvm.service.extract.fields.AggregateField;
import com.achilio.mvm.service.extract.fields.ExpressionField;
import com.achilio.mvm.service.extract.fields.ReferenceField;
import com.google.common.collect.ImmutableList;
import com.google.zetasql.Analyzer;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedColumn;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;

import java.util.ArrayList;
import java.util.List;

public class ZetaSQLFieldSetExtractGlobalVisitor extends ZetaSQLFieldSetExtractVisitor {

	ZetaSQLFieldSetExtractGlobalVisitor(SimpleCatalog catalog) {
		super(catalog);
	}

	@Override
	protected void defaultVisit(ResolvedNode node) {
		node.getClass();
		super.defaultVisit(node);
	}

	@Override
	public void visit(ResolvedNodes.ResolvedAggregateScan node) {
		super.visit(node);
	}

	@Override
	public void visit(ResolvedNodes.ResolvedOutputColumn node) {
		final String tableName = node.getColumn().getTableName();
		if (!tableName.startsWith("$")) {
			this.addField(new ReferenceField(node.getColumn().getName()));
		}
	}

	/**
	 * Visit reference column in the select clause
	 * ie: {@code SELECT col1, col2, ..., colN}
	 *
	 * @param node
	 */
	@Override
	public void visit(ResolvedNodes.ResolvedComputedColumn node) {
		if (node.getExpr() instanceof ResolvedNodes.ResolvedColumnRef) {
			ResolvedNodes.ResolvedColumnRef ref = (ResolvedNodes.ResolvedColumnRef) node.getExpr();
			ResolvedColumn column = ref.getColumn();
			this.addField(new ReferenceField(column.getName()));

		}  else if (node.getExpr() instanceof ResolvedNodes.ResolvedAggregateFunctionCall){
			ResolvedNodes.ResolvedAggregateFunctionCall func = (ResolvedNodes.ResolvedAggregateFunctionCall) node.getExpr();
			String expression = Analyzer.buildExpression(func, this.getCatalog());
			expression = hackMappingColumnsInFunction(expression, func);
			this.addField(new AggregateField(expression));

		} else if (node.getExpr() instanceof ResolvedNodes.ResolvedFunctionCallBase){
			ResolvedNodes.ResolvedFunctionCallBase func = (ResolvedNodes.ResolvedFunctionCallBase) node.getExpr();
			String expression = Analyzer.buildExpression(func, this.getCatalog());
			expression = hackMappingColumnsInFunction(expression, func);
			this.addField(new ExpressionField(expression));
		}
		super.visit(node);
	}

	private String hackMappingColumnsInFunction(String expression, ResolvedNodes.ResolvedFunctionCallBase expr) {
		List<ResolvedNodes.ResolvedColumnRef> refs = new ArrayList<>();
		hackFindColumnsRefInNode(expr.getArgumentList(), refs);
		int count = 0;
		for (ResolvedNodes.ResolvedColumnRef ref : refs) {
			// Hack in order to build the node with source columns name.
			expression = expression.replaceAll("a_" + ++count, ref.getColumn().getName());
		}
		return expression;
	}

	private void hackFindColumnsRefInNode(ImmutableList<ResolvedNodes.ResolvedExpr> exprs, List<ResolvedNodes.ResolvedColumnRef> refs) {
		for (ResolvedNodes.ResolvedExpr expr : exprs) {
			if (expr instanceof ResolvedNodes.ResolvedFunctionCallBase) {
				hackFindColumnsRefInNode(((ResolvedNodes.ResolvedFunctionCallBase) expr).getArgumentList(), refs);
			} else if (expr instanceof ResolvedNodes.ResolvedColumnRef) {
				refs.add((ResolvedNodes.ResolvedColumnRef) expr);
			}
		}
	}

	/**
	 * Visit the filter clause and visit nodes in expression.
	 * ie: {@code WHERE col = 'token'}
	 *
	 * @param node
	 */
	@Override
	public void visit(ResolvedNodes.ResolvedFilterScan node) {
		ZetaSQLFieldSetExtractFilterVisitor visitor = new ZetaSQLFieldSetExtractFilterVisitor(this.getCatalog());
		node.getFilterExpr().accept(visitor);
		this.merge(visitor.fieldSet());
		super.visit(node);
	}

}
