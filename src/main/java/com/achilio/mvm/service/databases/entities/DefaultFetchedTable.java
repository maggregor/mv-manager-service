package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.visitors.ATableId;
import java.util.Collections;
import java.util.Map;

public class DefaultFetchedTable implements FetchedTable {

  private ATableId tableId;

  private Map<String, String> columns;

  @Deprecated
  public DefaultFetchedTable(final String project, final String dataset, final String table) {
    this(ATableId.of(project, dataset, table));
  }

  @Deprecated
  public DefaultFetchedTable(
      final String project,
      final String dataset,
      final String table,
      final Map<String, String> columns) {
    this(ATableId.of(project, dataset, table), columns);
  }

  public DefaultFetchedTable(ATableId tableId) {
    this(tableId, Collections.emptyMap());
  }

  public DefaultFetchedTable(final ATableId tableId, final Map<String, String> columns) {
    this.tableId = tableId;
    this.columns = columns;
  }

  @Override
  public String getProjectId() {
    return this.tableId.getProject();
  }

  @Override
  public String getDatasetName() {
    return this.tableId.getDataset();
  }

  @Override
  public String getTableName() {
    return this.tableId.getTable();
  }

  @Override
  public ATableId getTableId() {
    return this.tableId;
  }

  @Override
  public Map<String, String> getColumns() {
    return this.columns;
  }
}
