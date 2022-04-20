package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.services.FetcherService;
import com.google.cloud.bigquery.Dataset;
import java.util.Collections;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class QueryFetcherJobConfiguration extends DefaultBatchConfigurer {

  private static final Logger LOGGER =
      Logger.getLogger(QueryFetcherJobConfiguration.class.getName());

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  private final EntityManagerFactory emf;
  private final FetcherService fetcherService;

  private final DefaultJpaTransactionManager jpaTransactionManager;

  public QueryFetcherJobConfiguration(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      EntityManagerFactory emf,
      FetcherService fetcherService,
      DefaultJpaTransactionManager jpaTransactionManager) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.emf = emf;
    this.fetcherService = fetcherService;
    this.jpaTransactionManager = jpaTransactionManager;
  }

  @Bean
  @StepScope
  public IteratorItemReader<com.google.cloud.bigquery.Job> reader(
      @Value("#{jobParameters['projectId']}") String projectId,
      @Value("#{jobParameters['timeframe']}") int timeframe) {
    LOGGER.info(projectId);
    if (projectId != null) {
      return new QueryFetcherJobReader(fetcherService.fetchJobIterable(projectId, timeframe));
    }
    return new IteratorItemReader<>(Collections.singletonList(null));
  }

  @Bean
  @StepScope
  public JpaItemWriter<Query> writer() {
    JpaItemWriter<Query> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }

  @Bean
  public JobParameters defaultJobParameters() {
    return new JobParametersBuilder().toJobParameters();
  }

  @Bean
  public Step fetchQueries(JpaItemWriter<Query> writer) {
    return stepBuilderFactory
        .get("retrieveQueries")
        .transactionManager(jpaTransactionManager.jpaTransactionManager())
        .<com.google.cloud.bigquery.Job, Query>chunk(1000)
        .reader(reader(null, 0))
        .processor(new BigQueryJobProcessor())
        .writer(writer)
        .build();
  }

  @Bean("fetchQueryJob")
  public Job fetchQueryJob(@Qualifier("fetchQueries") Step fetchQueries) {
    return this.jobBuilderFactory
        .get("fetchQueryJob1")
        .incrementer(new RunIdIncrementer())
        .start(fetchQueries)
        .build();
  }

  /**
   * This section is dedicated to Struct fetching
   */
  @Bean
  @StepScope
  public IteratorItemReader<Dataset> datasetReader(
      @Value("#{jobParameters['projectId']}") String projectId) {
    LOGGER.info(projectId);
    if (projectId != null) {
      return new IteratorItemReader<>(fetcherService.fetchAllDatasets(projectId));
    }
    return new IteratorItemReader<>(Collections.singletonList(null));
  }

  @Bean
  @StepScope
  public JpaItemWriter<ADataset> datasetWriter() {
    JpaItemWriter<ADataset> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }

  @Bean
  public Step fetchDatasets(JpaItemWriter<ADataset> writer) {
    return stepBuilderFactory
        .get("retrieveQueries")
        .transactionManager(jpaTransactionManager.jpaTransactionManager())
        .<Dataset, ADataset>chunk(1000)
        .reader(datasetReader(null))
        .processor(new BigQueryDatasetProcessor())
        .writer(writer)
        .build();
  }

  @Bean("fetchStructJob")
  public Job fetchStructJob(Step fetchDatasets) {
    return this.jobBuilderFactory
        .get("fetchStructJob1")
        .incrementer(new RunIdIncrementer())
        .start(fetchDatasets)
        .build();
  }
}
