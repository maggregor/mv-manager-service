package com.achilio.mvm.service.events;

import com.google.api.client.util.ArrayMap;
import java.util.Map;

public class QueryFetcherJobFinishedEvent extends Event {

  private String status;

  public QueryFetcherJobFinishedEvent(String teamName, String projectId) {
    super(Type.QUERY_FETCHER_JOB_FINISHED, teamName, projectId);
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public Map<String, String> getData() {
    Map<String, String> data = new ArrayMap<>();
    data.put("status", status);
    return data;
  }

}
