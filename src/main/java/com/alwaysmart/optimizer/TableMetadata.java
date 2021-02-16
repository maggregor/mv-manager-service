package com.alwaysmart.optimizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure of table.
 * Hold schema name, table name and column with their types and names.
 */
public class TableMetadata {

	private String schema;
	private String table;
	/* The columns of the table. Key = name, Value = Type */
	private Map<String, String> columns = new HashMap<>();

	public TableMetadata(String schema, String table) {
		this.schema = schema;
		this.table = table;
	}

	public String getSchema() {
		return this.schema;
	}

	public String getTable() {
		return this.table;
	}

	public Map<String, String> getColumns() {
		return this.columns;
	}

	public void addColumn(String name, String type) {
		columns.put(name, type);
	}

}
