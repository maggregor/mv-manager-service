package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.extract.fields.Field;
import com.achilio.mvm.service.extract.fields.FieldSet;

public interface MaterializedViewStatementBuilder {

  String build(FieldSet fieldSet);

  String serializeField(Field field, boolean addAlias);
}
