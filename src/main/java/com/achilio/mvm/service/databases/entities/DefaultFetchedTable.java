package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.visitors.TableId;
import java.util.Collections;
import java.util.Map;

public class DefaultFetchedTable implements FetchedTable {

  private TableId tableId;

  private Map<String, String> columns;

  @Deprecated
  public DefaultFetchedTable(final String project, final String dataset, final String table) {
    this(TableId.of(project, dataset, table));
  }

  @Deprecated
  public DefaultFetchedTable(final String project, final String dataset, final String table, final Map<String, String> columns) {
    this(TableId.of(project, dataset, table), columns);
  }

  public DefaultFetchedTable(TableId tableId) {
    this(tableId, Collections.emptyMap());
  }

  public DefaultFetchedTable(final TableId tableId, final Map<String, String> columns) {
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
  public TableId getTableId() {
    return this.tableId;
  }

  @Override
  public Map<String, String> getColumns() {
    return this.columns;
  }
}
