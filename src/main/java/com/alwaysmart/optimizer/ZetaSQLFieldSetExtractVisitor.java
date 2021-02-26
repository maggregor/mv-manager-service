package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.fields.Field;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZetaSQLFieldSetExtractVisitor  extends ResolvedNodes.Visitor implements FieldSetExtractVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLFieldSetExtractVisitor.class);
	private static final String COLUMN_PREFIX_TO_SKIP = "$";

	private FieldSet fieldSet = new DefaultFieldSet();
	private SimpleCatalog catalog;

	@Override
	protected void defaultVisit(ResolvedNode node) {
		super.defaultVisit(node);
	}


	ZetaSQLFieldSetExtractVisitor(SimpleCatalog catalog) {
		this.catalog = catalog;
	}

	public void addField(Field field) {
		this.fieldSet.add(field);
	}

	public void merge(FieldSet fieldSet) {
		this.fieldSet.merge(fieldSet);
	}

	@Override
	public FieldSet fieldSet() {
		this.fieldSet.fields().removeIf(field -> field.name().startsWith(COLUMN_PREFIX_TO_SKIP));
		return fieldSet;
	}

	public SimpleCatalog getCatalog() {
		return this.catalog;
	}

}
