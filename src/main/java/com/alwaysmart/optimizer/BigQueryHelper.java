package com.alwaysmart.optimizer;

import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.TableId;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BigQueryHelper {

	public static DatasetId parseDataset(String datasetId) {
		String[] split = datasetId.split("\\.");
		if (split.length < 2) {
			throw new IllegalArgumentException("The dataset id should be projectName.datasetName");
		}
		return DatasetId.of(split[0], split[1]);
	}

	public static TableId parseTable(String tableId) {
		String[] split = tableId.split("\\.");
		return parseTable(split);
	}

	public static TableId parseTable(List<String> tableId) {
		return parseTable(tableId.toArray(new String[0]));
	}

	public static TableId parseTable(String[] tableId) {
		if (tableId.length == 2) {
			return TableId.of(tableId[0], tableId[1]);
		} else if (tableId.length == 3) {
			return TableId.of(tableId[0], tableId[1], tableId[2]);
		}
		throw new IllegalArgumentException(
				"The table id should be projectName.datasetName.tableName or datasetName.tableName");
	}

	public static String datasetToString(DatasetId tableId) {
		return String.format("%s.%s", tableId.getProject(), tableId.getDataset());
	}

	public static String tableToString(TableId tableId) {
		if (StringUtils.isBlank(tableId.getProject())) {
			return String.format("%s.%s", tableId.getDataset(), tableId.getTable());
		}
		return String.format("%s.%s.%s", tableId.getProject(), tableId.getDataset(), tableId.getTable());
	}

	public static List<String> parseTableIdToPath(String tableIdString) {
		TableId tableId = parseTable(tableIdString);
		List<String> tablePath = new ArrayList<>();
		if (tableId.getProject() != null) {
			tablePath.add(tableId.getProject());
		}
		tablePath.add(tableId.getDataset());
		tablePath.add(tableId.getTable());
		return tablePath;
	}
}
