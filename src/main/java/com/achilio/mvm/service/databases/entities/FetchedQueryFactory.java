package com.achilio.mvm.service.databases.entities;

public enum FetchedQueryFactory {
  ;

  public static FetchedQuery createFetchedQuery(String statement) {
    return new FetchedQuery(statement);
  }
}
