package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.ReferenceField;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNodes;

public class ZetaSQLFieldSetExtractFilterVisitor extends ZetaSQLFieldSetExtractVisitor {

	public ZetaSQLFieldSetExtractFilterVisitor(SimpleCatalog catalog) {
		super(catalog);
	}

	@Override
	public void visit(ResolvedNodes.ResolvedColumnRef node) {
		final String referenceName = node.getColumn().getName();
		this.addField(new ReferenceField(referenceName));
		super.visit(node);
	}
}
