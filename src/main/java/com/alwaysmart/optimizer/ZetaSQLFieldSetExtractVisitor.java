package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.AggregateField;
import com.alwaysmart.optimizer.fields.ReferenceField;
import com.google.zetasql.resolvedast.ResolvedNodes;

public class ZetaSQLFieldSetExtractVisitor extends FieldSetExtractVisitor {

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
	public void visit(ResolvedNodes.ResolvedAggregateFunctionCall node) {
		this.addField(new AggregateField(node.getFunction().getSqlName() +  "("
				+ ((ResolvedNodes.ResolvedColumnRef)node.getArgumentList().get(0)).getColumn().getName() + ")"));
		super.visit(node);
	}

	/**
	 * Visit the filter clause and visit nodes in expression.
	 * ie: {@code WHERE col = 'token'}
	 *
	 * @param node
	 */
	@Override
	public void visit(ResolvedNodes.ResolvedFilterScan node) {
		ZetaSQLFieldSetExtractFilterVisitor visitor = new ZetaSQLFieldSetExtractFilterVisitor();
		node.getFilterExpr().accept(visitor);
		this.merge(visitor.fieldSet());
		super.visit(node);
	}

}
