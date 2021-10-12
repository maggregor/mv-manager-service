package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.extract.fields.FieldSet;

public interface MaterializedViewStatementBuilder {

  String build(FieldSet fieldSet);
}
