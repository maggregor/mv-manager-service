package com.alwaysmart.optimizer;

public class DefaultFetchedQuery implements FetchedQuery {

	private String statement;
	private long cost;

	public DefaultFetchedQuery(final String statement,
							   final long cost) {
		this.statement = statement;
		this.cost = cost;
	}

	public long cost() {
		return cost;
	}

	public String statement() {
		return statement;
	}

}
