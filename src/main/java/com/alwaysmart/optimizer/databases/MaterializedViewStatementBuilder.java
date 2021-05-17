package com.alwaysmart.optimizer.databases;

import com.alwaysmart.optimizer.extract.fields.Field;
import com.alwaysmart.optimizer.extract.fields.FieldSet;

public interface MaterializedViewStatementBuilder {

	String build(FieldSet fieldSet);

	String serializeField(Field field, boolean addAlias);
}
