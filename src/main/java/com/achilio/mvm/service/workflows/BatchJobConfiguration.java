package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.services.FetcherService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration extends DefaultBatchConfigurer {
  private static final Logger LOGGER = Logger.getLogger(BatchJobConfiguration.class.getName());

  @Autowired private JobBuilderFactory jobBuilderFactory;
  @Autowired private StepBuilderFactory stepBuilderFactory;

  @Autowired private EntityManagerFactory emf;
  @Autowired private FetcherService fetcherService;
  @Autowired private JobParameters jobParameters;
  @Autowired private QueryRepository queryRepository;
  @Autowired private DataSource dataSource;

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
      @Value("#{jobParameters['projectId']}") String projectId) {
    LOGGER.info(projectId);
    if (projectId != null) {
      return new FetcherQueryReader(fetcherService.fetchJobIterable(projectId));
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
  public Step step1(JpaItemWriter<Query> writer) {
    return stepBuilderFactory
        .get("fetchQueries")
        .transactionManager(jpaTransactionManager())
        .<com.google.cloud.bigquery.Job, Query>chunk(1000)
        .reader(reader(null))
        .processor(new BigQueryJobProcessor())
        .writer(writer)
        .build();
  }

  @Bean
  public Job fetchQueryJob(Step step1) {
    return this.jobBuilderFactory
        .get("fetchQueryJob1")
        .incrementer(new RunIdIncrementer())
        .start(step1)
        .build();
  }
}
