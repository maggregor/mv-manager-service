package com.achilio.mvm.service.events;

import org.springframework.batch.core.JobExecution;

public class QueryFetcherJobFinishedEvent extends JobExecutionEvent {

  public QueryFetcherJobFinishedEvent(JobExecution jobExecution) {
    super(Type.QUERY_FETCHER_JOB_FINISHED, jobExecution);
  }

}
