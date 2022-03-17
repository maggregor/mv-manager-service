package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import java.util.Set;

public interface ModelBuilder {

  default void registerTables(Set<FetchedTable> tables) {
    tables.forEach(this::registerTable);
  }

  void registerTable(FetchedTable table);

  boolean isTableRegistered(FetchedTable table);

  Set<FetchedTable> getTables();
}
