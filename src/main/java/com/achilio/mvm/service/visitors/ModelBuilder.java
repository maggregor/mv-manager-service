package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.ATable;
import java.util.Set;

public interface ModelBuilder {

  default void registerTables(Set<ATable> tables) {
    tables.forEach(this::registerTable);
  }

  void registerTable(ATable table);

  boolean isTableRegistered(ATable table);

  Set<ATable> getTables();
}
