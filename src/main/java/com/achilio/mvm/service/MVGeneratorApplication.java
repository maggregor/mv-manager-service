package com.achilio.mvm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableConfigurationProperties
@EnableSwagger2
@EnableBatchProcessing
public class MVGeneratorApplication implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(MVGeneratorApplication.class);

  @Value("${server.port}")
  private int serverPort;

  public static void main(String[] args) {
    SpringApplication.run(MVGeneratorApplication.class, args);
  }

  @Override
  public void run(String... args) {
    LOGGER.info("Server Port:" + serverPort);
  }

  @Bean
  public Docket productApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.achilio.mvm.service"))
        .build();
  }
}
