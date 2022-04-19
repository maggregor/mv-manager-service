// package com.achilio.mvm.service.workflows;
//
// import com.achilio.mvm.service.entities.ADataset;
// import com.achilio.mvm.service.services.FetcherService;
// import com.google.cloud.bigquery.Dataset;
// import java.util.Collections;
// import java.util.logging.Logger;
// import javax.persistence.EntityManagerFactory;
// import org.springframework.batch.core.Job;
// import org.springframework.batch.core.Step;
// import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
// import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
// import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
// import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
// import org.springframework.batch.core.configuration.annotation.StepScope;
// import org.springframework.batch.core.launch.support.RunIdIncrementer;
// import org.springframework.batch.item.database.JpaItemWriter;
// import org.springframework.batch.item.support.IteratorItemReader;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// @Configuration
// @EnableBatchProcessing
// public class StructFetcherJobConfiguration extends DefaultBatchConfigurer {
//
//  private static final Logger LOGGER =
//      Logger.getLogger(StructFetcherJobConfiguration.class.getName());
//  private final JobBuilderFactory jobBuilderFactory;
//  private final StepBuilderFactory stepBuilderFactory;
//
//  private final EntityManagerFactory emf;
//  private final FetcherService fetcherService;
//  private final DefaultJpaTransactionManager defaultJpaTransactionManager;
//
//  public StructFetcherJobConfiguration(
//      JobBuilderFactory jobBuilderFactory,
//      StepBuilderFactory stepBuilderFactory,
//      EntityManagerFactory emf,
//      FetcherService fetcherService,
//      DefaultJpaTransactionManager defaultJpaTransactionManager) {
//    this.jobBuilderFactory = jobBuilderFactory;
//    this.stepBuilderFactory = stepBuilderFactory;
//    this.emf = emf;
//    this.fetcherService = fetcherService;
//    this.defaultJpaTransactionManager = defaultJpaTransactionManager;
//  }
//
//  @Bean
//  @StepScope
//  public IteratorItemReader<Dataset> datasetReader(
//      @Value("#{jobParameters['projectId']}") String projectId) {
//    LOGGER.info(projectId);
//    if (projectId != null) {
//      return new DatasetFetcherJobReader(fetcherService.fetchAllDatasets(projectId));
//    }
//    return new IteratorItemReader<>(Collections.singletonList(null));
//  }
//
//  @Bean
//  @StepScope
//  public JpaItemWriter<ADataset> datasetWriter() {
//    JpaItemWriter<ADataset> writer = new JpaItemWriter<>();
//    writer.setEntityManagerFactory(emf);
//    return writer;
//  }
//
//  @Bean
//  public Step retrieveDatasets(JpaItemWriter<ADataset> writer) {
//    return stepBuilderFactory
//        .get("retrieveQueries")
//        .transactionManager(defaultJpaTransactionManager.jpaTransactionManager())
//        .<Dataset, ADataset>chunk(1000)
//        .reader(datasetReader(null))
//        .processor(new BigQueryDatasetProcessor())
//        .writer(writer)
//        .build();
//  }
//
//  @Bean
//  public Job fetchStructJob(Step retrieveDatasets) {
//    return this.jobBuilderFactory
//        .get("fetchStructJob1")
//        .incrementer(new RunIdIncrementer())
//        .start(retrieveDatasets)
//        .build();
//  }
//
//  //  @Bean
//  //  @StepScope
//  //  public IteratorItemReader<Table> tableReader(
//  //      @Value("#{jobParameters['projectId']}") String projectId) {
//  //    LOGGER.info(projectId);
//  //    if (projectId != null) {
//  //      return new TableFetcherJobReader(fetcherService.fetchAllTables(projectId));
//  //    }
//  //    return new IteratorItemReader<>(Collections.singletonList(null));
//  //  }
//
// }
