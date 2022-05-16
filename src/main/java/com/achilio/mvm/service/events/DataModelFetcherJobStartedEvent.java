package com.achilio.mvm.service.events;

import org.springframework.batch.core.JobExecution;

public class DataModelFetcherJobStartedEvent extends JobExecutionEvent {

  public DataModelFetcherJobStartedEvent(JobExecution jobExecution) {
    super(Type.DATA_MODEL_FETCHER_JOB_STARTED, jobExecution);
  }

}
