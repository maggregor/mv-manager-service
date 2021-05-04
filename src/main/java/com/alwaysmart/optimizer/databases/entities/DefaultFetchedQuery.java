package com.alwaysmart.optimizer.databases.entities;

import com.google.cloud.bigquery.TableId;

public class DefaultFetchedQuery implements FetchedQuery {

	private TableId tableId;
	private String statement;
	private long cost;

	public DefaultFetchedQuery(final String statement,
							   final long cost) {
		this.statement = statement;
		this.cost = cost;
	}

	public void setTableId(TableId tableId) {
		this.tableId = tableId;
	}

	public TableId getTableId() {
		return this.tableId;
	}

	public long cost() {
		return cost;
	}

	public String statement() {
		return statement;
	}

}
