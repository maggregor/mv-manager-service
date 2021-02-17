package com.alwaysmart.optimizer;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

import com.alwaysmart.optimizer.controllers.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

//Excluding Spring default Security Configuration.
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableConfigurationProperties
public class OptimizerApplication implements CommandLineRunner {


	private static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

	@Autowired
	private Environment env;

	@Value("${server.port}")
	private int serverPort;

	@Value("${fullname.firstname}")
	private String firstName;

	public static void main(String[] args) {
		SpringApplication.run(OptimizerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("FirstName using environment :" + env.getProperty("fullname.firstname"));
		LOGGER.info("FirstName using @Value tag: " + firstName);
		LOGGER.info("Server Port:" + serverPort);
	}

	/**
	 * Allowing to use custom.yaml properties in Spring Env.
	 * 
	 * @return PropertySource
	 */
	@Bean
	public static PropertySource<?> loadYamlPropertiesInSpringEnv(ConfigurableApplicationContext apContext)
			throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		PropertySource<?> applicationYamlPropertySource = loader
				.load("custom2.yml", new ClassPathResource("custom2.yml")).get(0);
		// It is necessary to add this in application context.
		apContext.getEnvironment().getPropertySources().addLast(applicationYamlPropertySource);
		return applicationYamlPropertySource;
	}

	/**
	 * This will give us freedom to use external configuration of yml/yaml file we
	 * are not talking about application.yml, we are talking about custom.yml file
	 * and we have to inject that value using @Value tag.
	 * 
	 * @return PropertySourcesPlaceholderConfigurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		// This is the main part which is used to load yaml files.
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		yaml.setResources(new ClassPathResource("custom2.yml"));
		propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
		return propertySourcesPlaceholderConfigurer;
	}

	/**
	 * Configuring i18n(Internalization)
	 * 
	 * @return LocaleResolver
	 */
	@Bean
	public LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		return localeResolver;
	}
}
