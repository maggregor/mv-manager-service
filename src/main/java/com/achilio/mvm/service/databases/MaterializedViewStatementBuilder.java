package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.visitors.fields.FieldSet;

public interface MaterializedViewStatementBuilder {

  String build(FieldSet queryPattern);
}
