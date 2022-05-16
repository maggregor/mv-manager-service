package com.achilio.mvm.service.events;

import org.springframework.batch.core.JobExecution;

public class QueryFetcherJobStartedEvent extends JobExecutionEvent {

  public QueryFetcherJobStartedEvent(JobExecution jobExecution) {
    super(Type.QUERY_FETCHER_JOB_STARTED, jobExecution);
  }
}
