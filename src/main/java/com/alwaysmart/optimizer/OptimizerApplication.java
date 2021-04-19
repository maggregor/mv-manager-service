package com.alwaysmart.optimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableConfigurationProperties
public class OptimizerApplication implements CommandLineRunner {


	private static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

	@Autowired
	private Environment env;

	@Value("${server.port}")
	private int serverPort;

	public static void main(String[] args) {
		SpringApplication.run(OptimizerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("Server Port:" + serverPort);
	}
}
