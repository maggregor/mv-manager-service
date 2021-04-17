package com.alwaysmart.optimizer;

public enum FetchedQueryFactory {

		;

		public static FetchedQuery createFetchedQuery(String statement) {
			return createFetchedQuery(statement,0);
		}

		public static FetchedQuery createFetchedQuery(String statement, long cost) {
			return new DefaultFetchedQuery(statement, cost);
		}

}
