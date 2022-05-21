package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.entities.QueryPattern;

public interface MaterializedViewStatementBuilder {

  String build(QueryPattern queryPattern);
}
