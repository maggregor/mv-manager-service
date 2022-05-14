package com.achilio.mvm.service.services;

import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BatchJobService {

  private final JobLauncher jobLauncher;

  private final Job fetcherQueryJob;
  private final Job fetcherDataModelJob;
  //private final Job fetcherExtractJob

  public BatchJobService(JobLauncher jobLauncher,
      @Qualifier("fetcherDataModelJob") Job fetcherDataModel,
      @Qualifier("fetcherQueryJob") Job fetcherQueryJob) {
    this.jobLauncher = jobLauncher;
    this.fetcherDataModelJob = fetcherDataModel;
    this.fetcherQueryJob = fetcherQueryJob;
  }

  @Async("asyncExecutor")
  @SneakyThrows
  public void runFetcherQueryJob(String teamName, String projectId, long timeframe) {
    JobParameters jobParameters = baseParamsBuilder(teamName, projectId)
        .addLong("timeframe", timeframe)
        .toJobParameters();
    jobLauncher.run(fetcherQueryJob, jobParameters);
  }

  @Async("asyncExecutor")
  @SneakyThrows
  public void runFetcherDataModelJob(String teamName, String projectId) {
    jobLauncher.run(fetcherDataModelJob, baseParamsBuilder(teamName, projectId).toJobParameters());
  }

  @Async("asyncExecutor")
  @SneakyThrows
  public void runFetcherExtractJob(String teamName, String projectId) {
    throw new IllegalArgumentException("Extract job is not yet supported");
  }

  private JobParametersBuilder baseParamsBuilder(String teamName, String projectId) {
    return new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .addString("projectId", projectId)
        .addString("teamName", teamName);
  }

}
