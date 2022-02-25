package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.google.cloud.bigquery.TableId;
import java.util.HashMap;
import java.util.Map;

public class BigQueryHelper {

  public static TableId parseTable(String tableId) {
    String[] split = tableId.split("\\.");
    return parseTable(split);
  }

  public static TableId parseTable(String[] tableId) {
    if (tableId.length == 1) {
      if (tableId[0].contains(".")) {
        tableId = tableId[0].split("\\.");
      }
    }
    if (tableId.length == 2) {
      return TableId.of(tableId[0], tableId[1]);
    } else if (tableId.length == 3) {
      return TableId.of(tableId[0], tableId[1], tableId[2]);
    }
    throw new IllegalArgumentException(
        "The table id should be projectName.datasetName.tableName or datasetName.tableName");
  }

  public static class FetchedTableHelper {

    public static FetchedTable createFetchedTable(String tableIdString, String[]... columns) {
      TableId tableId = parseTable(tableIdString);
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
}
