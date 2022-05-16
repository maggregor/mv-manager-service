package com.achilio.mvm.service.events;

import org.springframework.batch.core.JobExecution;

public class DataModelFetcherJobFinishedEvent extends JobExecutionEvent {

  public DataModelFetcherJobFinishedEvent(JobExecution jobExecution) {
    super(Type.DATA_MODEL_FETCHER_JOB_FINISHED, jobExecution);
  }

}
