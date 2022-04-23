package com.achilio.mvm.service.workflows;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.context.annotation.Bean;

public class DefaultJobParameters extends JobParameters {

  @Bean
  public JobParameters defaultJobParameters() {
    return new JobParametersBuilder().toJobParameters();
  }
}
