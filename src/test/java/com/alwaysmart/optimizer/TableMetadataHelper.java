package com.alwaysmart.optimizer;

import java.util.HashMap;
import java.util.Map;

public class TableMetadataHelper {

	public static TableMetadata createTableMetadata(String datasetName, String tableName, String[] ...columns) {
		Map<String, String> columnMap = new HashMap<>();
		for (String[] col : columns) {
			columnMap.put(col[0], col[1]);
		}
		return new DefaultTableMetadata(datasetName, tableName, columnMap);
	}

}
