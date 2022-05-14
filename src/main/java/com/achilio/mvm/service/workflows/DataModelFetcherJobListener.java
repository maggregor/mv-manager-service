package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.events.DataModelFetcherJobFinishedEvent;
import com.achilio.mvm.service.events.DataModelFetcherJobStartedEvent;
import com.achilio.mvm.service.services.PublisherService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class DataModelFetcherJobListener implements JobExecutionListener {

  private final PublisherService publisherService;

  public DataModelFetcherJobListener(
      PublisherService publisherService) {
    this.publisherService = publisherService;
  }

  public void beforeJob(JobExecution jobExecution) {
    publisherService.handleEvent(new DataModelFetcherJobStartedEvent(jobExecution));
  }

  public void afterJob(JobExecution jobExecution) {
    publisherService.handleEvent(new DataModelFetcherJobFinishedEvent(jobExecution));
  }
}