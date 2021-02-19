package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.ReferenceField;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;

public class ZetaSQLFieldSetExtractVisitor extends ResolvedNodes.Visitor {

	private static final String COLUMN_PREFIX_TO_SKIP = "$";
	private FieldSet fieldSet = new DefaultFieldSet();

	protected void defaultVisit(ResolvedNode node) {
		super.defaultVisit(node);
	}

	/**
	 * Visit reference column in the select clause
	 * ie: {@code SELECT col1, col2, ..., colN}
	 *
	 * @param node
	 */
	public void visit(ResolvedNodes.ResolvedOutputColumn node) {
		fieldSet.add(new ReferenceField(node.getName()));
		super.visit(node);
	}

	public void visit(ResolvedNodes.ResolvedColumnRef node) {
		fieldSet.add(new ReferenceField(node.getColumn().getName()));
		super.visit(node);
	}

	/**
	 * Visit the filter clause and visit nodes in expression.
	 * ie: {@code WHERE col = 'token'}
	 *
	 * @param node
	 */
	public void visit(ResolvedNodes.ResolvedFilterScan node) {
		node.getFilterExpr().accept(this);
		super.visit(node);
	}

	public FieldSet fields() {
		this.fieldSet.fields().removeIf(field -> field.name().startsWith(COLUMN_PREFIX_TO_SKIP));
		return this.fieldSet;
	}
}
