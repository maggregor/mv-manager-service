package com.achilio.mvm.service.events;

public class QueryFetcherJobStartedEvent extends Event {

  public QueryFetcherJobStartedEvent(String teamName, String projectId) {
    super(Type.QUERY_FETCHER_JOB_STARTED, teamName, projectId);
  }

  @Override
  public Object getData() {
    return null;
  }
}
