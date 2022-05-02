package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.events.QueryFetcherJobFinishedEvent;
import com.achilio.mvm.service.events.QueryFetcherJobStartedEvent;
import com.achilio.mvm.service.services.PublisherService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;

public class QueryFetcherJobListener implements JobExecutionListener {

  private final PublisherService publisherService;

  public QueryFetcherJobListener(
      PublisherService publisherService) {
    this.publisherService = publisherService;
  }

  public void beforeJob(JobExecution jobExecution) {
    JobParameters params = jobExecution.getJobParameters();
    final String teamName = params.getString("teamName");
    final String projectId = params.getString("projectId");
    publisherService.handleEvent(new QueryFetcherJobStartedEvent(teamName, projectId));
  }

  public void afterJob(JobExecution jobExecution) {
    JobParameters params = jobExecution.getJobParameters();
    final String teamName = params.getString("teamName");
    final String projectId = params.getString("projectId");
    QueryFetcherJobFinishedEvent event = new QueryFetcherJobFinishedEvent(teamName,
        projectId);
    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
      event.setStatus(BatchStatus.COMPLETED.name());
    } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
      event.setStatus(BatchStatus.FAILED.name());
    }
    publisherService.handleEvent(event);
  }
}