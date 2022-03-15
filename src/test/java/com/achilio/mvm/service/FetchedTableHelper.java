package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.TableId;
import java.util.HashMap;
import java.util.Map;

public class FetchedTableHelper {

  public static FetchedTable createFetchedTable(String tableIdString, String[]... columns) {
    TableId tableId = TableId.parse(tableIdString);
    return createFetchedTable(
        tableId.getProject(), tableId.getDataset(), tableId.getTable(), columns);
  }

  public static FetchedTable createFetchedTable(TableId tableId, String[]... columns) {
    return createFetchedTable(
        tableId.getProject(), tableId.getDataset(), tableId.getTable(), columns);
  }

  public static FetchedTable createFetchedTable(
      String project, String datasetName, String tableName, String[]... columns) {
    Map<String, String> columnMap = new HashMap<>();
    for (String[] col : columns) {
      columnMap.put(col[0], col[1]);
    }
    return new DefaultFetchedTable(project, datasetName, tableName, columnMap);
  }
}
