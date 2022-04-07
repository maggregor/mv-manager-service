package com.achilio.mvm.service.exceptions;

public class QueryNotFoundException extends NotFoundException {

  public QueryNotFoundException(String queryId) {
    super(String.format("Query %s not found", queryId));
  }
}
