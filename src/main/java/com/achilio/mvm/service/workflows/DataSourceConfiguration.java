// package com.achilio.mvm.service.workflows;
//
// import javax.sql.DataSource;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.boot.jdbc.DataSourceBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// @Configuration
// public class DataSourceConfiguration {
//
//  @Bean("batchDataSource")
//  @ConfigurationProperties(prefix = "spring.datasource")
//  public DataSource batchDataSource() {
//    return DataSourceBuilder.create().build();
//  }
// }
