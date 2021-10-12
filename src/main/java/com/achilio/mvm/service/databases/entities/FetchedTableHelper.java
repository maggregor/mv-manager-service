package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.bigquery.BigQueryHelper;
import com.google.cloud.bigquery.TableId;
import java.util.HashMap;
import java.util.Map;

public class FetchedTableHelper {

  public static FetchedTable createFetchedTable(String tableIdString, String[]... columns) {
    TableId tableId = BigQueryHelper.parseTable(tableIdString);
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
