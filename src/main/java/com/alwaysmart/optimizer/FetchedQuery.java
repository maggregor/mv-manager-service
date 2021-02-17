package com.alwaysmart.optimizer;

/**
 * Represent a fetched query from data warehouse fetching process.
 * @see DatabaseFetcher
 */
public class FetchedQuery {

	// SQL Statement
	private String statement;
	// Statistics
	private long scannedBytes;

	public FetchedQuery(String statement) {
		this.statement = statement;
	}

	public long getScannedBytes() {
		return scannedBytes;
	}

	public void setScannedBytes(long scannedBytes) {
		this.scannedBytes = scannedBytes;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
}
