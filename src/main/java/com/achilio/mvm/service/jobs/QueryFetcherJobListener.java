package com.achilio.mvm.service.jobs;

import com.achilio.mvm.service.events.QueryFetcherJobFinishedEvent;
import com.achilio.mvm.service.events.QueryFetcherJobStartedEvent;
import com.achilio.mvm.service.services.PublisherService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class QueryFetcherJobListener implements JobExecutionListener {

  private final PublisherService publisherService;

  public QueryFetcherJobListener(
      PublisherService publisherService) {
    this.publisherService = publisherService;
  }

  public void beforeJob(JobExecution jobExecution) {
    publisherService.handleEvent(new QueryFetcherJobStartedEvent(jobExecution));
  }

  public void afterJob(JobExecution jobExecution) {
    publisherService.handleEvent(new QueryFetcherJobFinishedEvent(jobExecution));
  }
}