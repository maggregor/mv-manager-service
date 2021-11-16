package com.achilio.mvm.service.databases.bigquery;

import com.google.cloud.bigquery.TableId;

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

}
