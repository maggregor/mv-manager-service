package com.achilio.mvm.service.services;

import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchJobService {

  private final JobLauncher jobLauncher;

  private final Job fetcherQueryJob;
  private final Job fetcherDataModelJob;
  private final Job extractQueryPatternJob;

  public BatchJobService(JobLauncher jobLauncher,
      @Qualifier("fetcherQueryJob") Job fetcherQueryJob,
      @Qualifier("fetcherDataModelJob") Job fetcherDataModelJob,
      @Qualifier("extractQueryPatternJob") Job extractQueryPatternJob) {
    this.jobLauncher = jobLauncher;
    this.fetcherQueryJob = fetcherQueryJob;
    this.fetcherDataModelJob = fetcherDataModelJob;
    this.extractQueryPatternJob = extractQueryPatternJob;
  }

  @SneakyThrows
  public void runFetcherQueryJob(String teamName, String projectId, long timeframe) {
    JobParameters jobParameters = baseParamsBuilder(teamName, projectId)
        .addLong("timeframe", timeframe)
        .toJobParameters();
    jobLauncher.run(fetcherQueryJob, jobParameters);
  }

  @SneakyThrows
  public void runFetcherDataModelJob(String teamName, String projectId) {
    jobLauncher.run(fetcherDataModelJob, baseParamsBuilder(teamName, projectId).toJobParameters());
  }

  @SneakyThrows
  public void runFetcherExtractJob(String teamName, String projectId) {
    jobLauncher.run(extractQueryPatternJob,
        baseParamsBuilder(teamName, projectId).toJobParameters());
  }

  private JobParametersBuilder baseParamsBuilder(String teamName, String projectId) {
    return new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .addString("projectId", projectId)
        .addString("teamName", teamName);
  }

}
