package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.AggregateField;
import com.alwaysmart.optimizer.fields.ReferenceField;
import com.google.common.collect.ImmutableList;
import com.google.zetasql.Analyzer;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNodes;

import java.util.ArrayList;
import java.util.List;

public class ZetaSQLFieldSetExtractGlobalVisitor extends ZetaSQLFieldSetExtractVisitor {

	ZetaSQLFieldSetExtractGlobalVisitor(SimpleCatalog catalog) {
		super(catalog);
	}

	/**
	 * Visit reference column in the select clause
	 * ie: {@code SELECT col1, col2, ..., colN}
	 *
	 * @param node
	 */
	@Override
	public void visit(ResolvedNodes.ResolvedOutputColumn node) {
		final String columnName = node.getColumn().getName();
		this.addField(new ReferenceField(columnName));
		super.visit(node);
	}


	@Override
	public void visit(ResolvedNodes.ResolvedAggregateScan node) {
		super.visit(node);
	}


	@Override
	public void visit(ResolvedNodes.ResolvedFunctionCall node) {
		super.visit(node);
	}

	@Override
	public void visit(ResolvedNodes.ResolvedFunctionArgument node) {
		super.visit(node);
	}

	@Override
	public void visit(ResolvedNodes.ResolvedCreateFunctionStmt node) {
		super.visit(node);
	}


	@Override
	public void visit(ResolvedNodes.ResolvedAggregateFunctionCall node) {
		String expression = Analyzer.buildExpression(node, this.getCatalog());
		List<ResolvedNodes.ResolvedColumnRef> refs = new ArrayList<>();
		hackFindColumnsRefInNode(node.getArgumentList(), refs);
		int count = 0;
		for (ResolvedNodes.ResolvedColumnRef ref : refs) {
			// Hack in order to build the node with source columns name.
			expression = expression.replaceAll("a_" + ++count, ref.getColumn().getName());
		}
		this.addField(new AggregateField(expression));
		super.visit(node);
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
