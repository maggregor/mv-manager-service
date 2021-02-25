package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.fields.Field;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.google.zetasql.resolvedast.ResolvedNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldSetExtractVisitor extends ResolvedNodes.Visitor{

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldSetExtractVisitor.class);
	private static final String COLUMN_PREFIX_TO_SKIP = "$";

	private FieldSet fieldSet = new DefaultFieldSet();

	public void addField(Field field) {
		this.fieldSet.add(field);
	}

	public void merge(FieldSet fieldSet) {
		this.fieldSet.merge(fieldSet);
	}

	public FieldSet fieldSet() {
		this.fieldSet.fields().removeIf(field -> field.name().startsWith(COLUMN_PREFIX_TO_SKIP));
		return this.fieldSet;
	}

}
