package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.services.FetcherService;
import com.google.cloud.bigquery.Dataset;
import java.util.Collections;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration extends DefaultBatchConfigurer {

  private static final Logger LOGGER =
      Logger.getLogger(BatchJobConfiguration.class.getName());

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  private final EntityManagerFactory emf;
  private final FetcherService fetcherService;
  private final DataSource dataSource;

  public BatchJobConfiguration(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      EntityManagerFactory emf,
      FetcherService fetcherService,
      DataSource dataSource) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.emf = emf;
    this.fetcherService = fetcherService;
    this.dataSource = dataSource;
  }

  @Bean
  @Primary
  public JpaTransactionManager jpaTransactionManager() {
    final JpaTransactionManager tm = new JpaTransactionManager();
    tm.setDataSource(dataSource);
    return tm;
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
  public JpaItemWriter<AQuery> writer() {
    JpaItemWriter<AQuery> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }

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
  public JobParameters defaultJobParameters() {
    return new JobParametersBuilder().toJobParameters();
  }

  @Bean
  public Step retrieveQueries(JpaItemWriter<AQuery> writer) {
    return stepBuilderFactory
        .get("retrieveQueries")
        .transactionManager(jpaTransactionManager())
        .<com.google.cloud.bigquery.Job, AQuery>chunk(1000)
        .reader(reader(null, 0))
        .processor(new BigQueryJobProcessor())
        .writer(writer)
        .build();
  }

  @Bean
  public Step retrieveDatasets(FetcherService fetcherService,
      ADatasetRepository datasetRepository) {
    return stepBuilderFactory
        .get("retrieveDatasets")
        .transactionManager(jpaTransactionManager())
        .<Dataset, ADataset>chunk(10)
        .reader(datasetReader(null))
        .processor(new BigQueryDatasetProcessor(fetcherService))
        .writer(new ADatasetItemWriter(datasetRepository))
        .build();
  }


  @Bean("fetchQueryJob")
  public Job fetchQueryJob(Step retrieveQueries) {
    return this.jobBuilderFactory
        .get("fetchQueryJob")
        .incrementer(new RunIdIncrementer())
        .start(retrieveQueries)
        .build();
  }

  @Bean("fetchDatasetsJob")
  public Job fetchStruct(Step retrieveDatasets) {
    return this.jobBuilderFactory
        .get("fetchDatasetsJob")
        .incrementer(new RunIdIncrementer())
        .start(retrieveDatasets)
        .build();
  }
}