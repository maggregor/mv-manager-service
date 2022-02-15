package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import java.util.Set;

public interface ModelBuilder {

  void registerTables(Set<FetchedTable> tables);

  void registerTable(FetchedTable table);

  boolean isTableRegistered(FetchedTable table);
}
