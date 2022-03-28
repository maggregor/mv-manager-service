package com.achilio.mvm.service.exceptions;

public class QueryNotFoundException extends IllegalArgumentException {

  public QueryNotFoundException(String queryId) {
    super(String.format("Query %s not found", queryId));
  }
}
