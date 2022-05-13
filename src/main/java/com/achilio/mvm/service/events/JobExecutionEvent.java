package com.achilio.mvm.service.events;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

public abstract class JobExecutionEvent extends Event {

  private static final String KEY_STATUS = "status";

  public JobExecutionEvent(Type eventType, JobExecution jobExecution) {
    super(eventType);
    JobParameters params = jobExecution.getJobParameters();
    final String teamName = params.getString("teamName");
    final String projectId = params.getString("projectId");
    this.addData(KEY_STATUS, jobExecution.getStatus().name().toLowerCase());
    this.setEventType(eventType);
    this.setTeamName(teamName);
    this.setProjectId(projectId);
  }

}
