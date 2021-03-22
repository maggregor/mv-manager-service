package com.alwaysmart.optimizer;

import java.util.Map;
import java.util.Objects;

public class DefaultTableMetadata implements TableMetadata {

	/**
	 * Table Metadata object.
	 *
	 * @attribute datasetName - the targeted dataset
	 * @attribute tableName - the targeted table
	 * @attribute colums - map of the columns: key is the column name, value is the
	 *            data type
	 */
	private String schema;
	private String table;
	private Map<String, String> columns;
	// TODO: Replace map columns with a custom Columns object
	
	DefaultTableMetadata(final String schema, final String table, final Map<String, String> columns) {
		this.schema = schema;
		this.table = table;
		this.columns = columns;
	}

	@Override
	public String getSchema() {
		return this.schema;
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
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DefaultTableMetadata that = (DefaultTableMetadata) o;
		return Objects.equals(schema, that.schema) && Objects.equals(table, that.table)
				&& Objects.equals(columns, that.columns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(schema, table, columns);
	}
}
