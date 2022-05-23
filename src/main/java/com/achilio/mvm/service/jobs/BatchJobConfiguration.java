package com.achilio.mvm.service.jobs;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.QueryPatternRepository;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.services.PublisherService;
import com.achilio.mvm.service.services.QueryService;
import com.google.cloud.bigquery.Dataset;
import java.util.Collections;
import java.util.List;
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

  private static final Logger LOGGER = Logger.getLogger(BatchJobConfiguration.class.getName());

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  private final EntityManagerFactory emf;
  private final FetcherService fetcherService;
  private final QueryService queryService;
  private final DataSource dataSource;

  public BatchJobConfiguration(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      EntityManagerFactory emf,
      FetcherService fetcherService,
      QueryService queryService,
      DataSource dataSource) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.emf = emf;
    this.fetcherService = fetcherService;
    this.queryService = queryService;
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
    if (projectId != null) {
      return new IteratorItemReader<>(fetcherService.fetchAllDatasets(projectId));
    }
    return new IteratorItemReader<>(Collections.singletonList(null));
  }

  @Bean
  @StepScope
  public IteratorItemReader<AQuery> queryReader(
      @Value("#{jobParameters['projectId']}") String projectId) {
    if (projectId != null) {
      return new IteratorItemReader<>(queryService.getAllQueries(projectId));
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
  public Step retrieveDatasets(
      FetcherService fetcherService, ADatasetRepository datasetRepository) {
    return stepBuilderFactory
        .get("retrieveDatasets")
        .transactionManager(jpaTransactionManager())
        .<Dataset, ADataset>chunk(10)
        .reader(datasetReader(null))
        .processor(new BigQueryDatasetProcessor(fetcherService))
        .writer(new ADatasetItemWriter(datasetRepository))
        .build();
  }

  @Bean
  public Step extractQueryPattern(
      ProjectService projectService, QueryPatternRepository queryPatternRepository) {
    return stepBuilderFactory
        .get("extractQueryPattern")
        .transactionManager(jpaTransactionManager())
        .<AQuery, List<QueryPattern>>chunk(10)
        .reader(queryReader(null))
        .processor(new QueryExtractProcessor(projectService))
        .writer(new QueryPatternItemWriter(queryPatternRepository))
        .build();
  }

  @Bean("fetcherQueryJob")
  public Job fetcherQueryJob(Step retrieveQueries, PublisherService service) {
    return this.jobBuilderFactory
        .get("fetcherQueryJob")
        .incrementer(new RunIdIncrementer())
        .start(retrieveQueries)
        .listener(new QueryFetcherJobListener(service))
        .build();
  }

  @Bean("fetcherDataModelJob")
  public Job fetcherDataModelJob(Step retrieveDatasets, PublisherService service) {
    return this.jobBuilderFactory
        .get("fetcherDataModelJob")
        .incrementer(new RunIdIncrementer())
        .start(retrieveDatasets)
        .listener(new DataModelFetcherJobListener(service))
        .build();
  }

  @Bean("extractQueryPatternJob")
  public Job extractQueryPatternJob(Step extractQueryPattern) {
    return this.jobBuilderFactory
        .get("extractQueryPatternJob")
        .incrementer(new RunIdIncrementer())
        .start(extractQueryPattern)
        .build();
  }
}
