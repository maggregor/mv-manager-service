package com.alwaysmart.optimizer;

/**
 * Represent a fetched query from data warehouse fetching process.
 * @see DataWarehouseFetcher
 */
public class FetchedQuery {

	// SQL Statement
	private String sql;
	// Statistics
	private long scannedBytes;

	public FetchedQuery(String sql) {
		this.sql = sql;
	}

	public long getScannedBytes() {
		return scannedBytes;
	}

	public void setScannedBytes(long scannedBytes) {
		this.scannedBytes = scannedBytes;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
