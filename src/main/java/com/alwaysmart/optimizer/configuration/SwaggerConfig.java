package com.alwaysmart.optimizer.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configured Swagger 2.0
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private final Contact DEFAULT_CONTACT = new Contact("Atishay Jain", "www.atishay.com", "atjain@gmail.com");
	private final ApiInfo DEFAULT = new ApiInfo("Api Documentation", "Rest WebServices for User", "1.0", "urn:tos",
			DEFAULT_CONTACT, "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", new ArrayList<>());

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.consumes(new HashSet<>(Arrays.asList("application/xml", "application/json")))
				.produces(new HashSet<>(Arrays.asList("application/xml", "application/json"))).forCodeGeneration(true)
				.apiInfo(DEFAULT);
	}

	@Bean
	public LinkDiscoverers discoverers() {
		List<LinkDiscoverer> plugins = new ArrayList<>();
		plugins.add(new CollectionJsonLinkDiscoverer());
		return new LinkDiscoverers(SimplePluginRegistry.create(plugins));

	}
}
