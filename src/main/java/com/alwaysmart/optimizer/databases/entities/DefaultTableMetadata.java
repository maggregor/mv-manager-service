package com.alwaysmart.optimizer.databases.entities;

import java.util.Map;
import java.util.Objects;

public class DefaultTableMetadata implements TableMetadata {

	/**
	 * Table Metadata object.
	 *
	 * @attribute tableId - the targeted table id
	 * @attribute datasetName - the targeted dataset
	 * @attribute tableName - the targeted table
	 * @attribute colums - map of the columns: key is the column name, value is the
	 *            data type
	 */
	private String tableId;
	private String project;
	private String dataset;
	private String table;
	private Map<String, String> columns;

	public DefaultTableMetadata(final String tableId,
								final String project,
								final String schema,
								final String table,
								final Map<String, String> columns) {
		this.tableId = tableId;
		this.project = project;
		this.dataset = schema;
		this.table = table;
		this.columns = columns;
	}

	@Override
	public String getTableId() {
		return this.tableId;
	}

	@Override
	public String getProject() {
		return this.project;
	}

	@Override
	public String getDataset() {
		return this.dataset;
	}

	@Override
	public String getTable() {
		return this.table;
	}

	@Override
	public Map<String, String> getColumns() {
		return this.columns;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultTableMetadata that = (DefaultTableMetadata) o;
		return Objects.equals(tableId, that.tableId) &&
				Objects.equals(project, that.project) &&
				Objects.equals(dataset, that.dataset) &&
				Objects.equals(table, that.table) &&
				Objects.equals(columns, that.columns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableId, project, dataset, table, columns);
	}
}
