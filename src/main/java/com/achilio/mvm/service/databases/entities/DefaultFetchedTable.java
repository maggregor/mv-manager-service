package com.achilio.mvm.service.databases.entities;

import java.util.Map;
import java.util.Objects;

public class DefaultFetchedTable implements FetchedTable {

	/**
	 * Table Metadata object.
	 *
	 * @attribute datasetName - the targeted dataset
	 * @attribute tableName - the targeted table
	 * @attribute colums - map of the columns: key is the column name, value is the
	 *            data type
	 */
	private final String project;
	private final String dataset;
	private final String table;
	private final Map<String, String> columns;

	public DefaultFetchedTable(final String project,
								final String schema,
								final String table,
								final Map<String, String> columns) {
		this.project = project;
		this.dataset = schema;
		this.table = table;
		this.columns = columns;
	}

	@Override
	public String getProjectId() {
		return this.project;
	}

	@Override
	public String getDatasetName() {
		return this.dataset;
	}

	@Override
	public String getTableName() {
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
		DefaultFetchedTable that = (DefaultFetchedTable) o;
		return Objects.equals(project, that.project) &&
				Objects.equals(dataset, that.dataset) &&
				Objects.equals(table, that.table) &&
				Objects.equals(columns, that.columns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project, dataset, table, columns);
	}
}
