package com.alwaysmart.optimizer;

public class TableMetadataHelper {

	public static TableMetadata getSimpleTableMetadata(String datasetName, String tableName) {
		return new TableMetadata(datasetName, tableName);
	}

}
