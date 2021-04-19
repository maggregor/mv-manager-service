package com.alwaysmart.optimizer.databases.entities;

import com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper;
import com.google.cloud.bigquery.TableId;

import java.util.HashMap;
import java.util.Map;

public class TableMetadataHelper {

	public static TableMetadata createTableMetadata(String tableIdString, String[] ...columns) {
		TableId tableId = BigQueryHelper.parseTable(tableIdString);
		return createTableMetadata(tableIdString, tableId.getProject(), tableId.getDataset(), tableId.getTable(), columns);
	}

	public static TableMetadata createTableMetadata(String tableId, String project, String datasetName, String tableName, String[] ...columns) {
		Map<String, String> columnMap = new HashMap<>();
		for (String[] col : columns) {
			columnMap.put(col[0], col[1]);
		}
		return new DefaultTableMetadata(tableId, project, datasetName, tableName, columnMap);
	}

}
