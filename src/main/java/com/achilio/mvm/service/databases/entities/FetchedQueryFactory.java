package com.achilio.mvm.service.databases.entities;

public enum FetchedQueryFactory {
  ;

  public static FetchedQuery createFetchedQuery(String projectId, String statement) {
    return new FetchedQuery(projectId, statement);
  }
}
