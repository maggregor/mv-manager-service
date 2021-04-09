package com.alwaysmart.optimizer;

import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.TableId;

public class BigQueryHelper {

	public static DatasetId parseDataset(String datasetId) {
		String[] split = datasetId.split("\\.");
		if (split.length < 2) {
			throw new IllegalArgumentException("The dataset id should be projectName.datasetName");
		}
		return DatasetId.of(split[0], split[1]);
	}

	public static TableId parseTable(String datasetId) {
		String[] split = datasetId.split("\\.");
		if (split.length < 3) {
			throw new IllegalArgumentException("The table id should be projectName.datasetName.tableName");
		}
		return TableId.of(split[0], split[1], split[2]);
	}

	public static String datasetToString(DatasetId tableId) {
		return String.format("%s.%s", tableId.getProject(), tableId.getDataset());
	}

	public static String tableToString(TableId tableId) {
		return String.format("%s.%s.%s", tableId.getProject(), tableId.getDataset(), tableId.getTable());
	}

}
