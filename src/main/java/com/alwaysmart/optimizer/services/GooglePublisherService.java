package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.OptimizerApplication;
import com.alwaysmart.optimizer.configuration.SimpleGoogleCredentialsAuthentication;
import com.alwaysmart.optimizer.entities.Optimization;
import com.alwaysmart.optimizer.entities.OptimizationResult;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Component
public class GooglePublisherService {

	private static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

	@Autowired
	private FetcherService fetcherService;

	@Value("${publisher.google-project-id}")
	private String GOOGLE_PROJECT_ID = "achilio-dev";
	@Value("${publisher.google-topic-id}")
	private String GOOGLE_TOPIC_ID = "mvExecutorTxopic";
	private final static String ATTRIBUTE_CMD_TYPE = "cmdType";
	private final static String ATTRIBUTE_PROJECT_ID = "projectId";
	private final static String ATTRIBUTE_ACCESS_TOKEN = "accessToken";
	private final static String ATTRIBUTE_REGION_ID = "regionId";
	private final static String ATTRIBUTE_DATASET_NAME = "datasetName";

	private final TopicName TOPIC_NAME = TopicName.of(GOOGLE_PROJECT_ID, GOOGLE_TOPIC_ID);;

	public void publishOptimization(Optimization optimization, List<OptimizationResult> results) throws IOException, ExecutionException, InterruptedException {
		if (results.isEmpty()) {
			LOGGER.info("No optimizations published because no results.");
			return;
		}
		final SimpleGoogleCredentialsAuthentication authentication = (SimpleGoogleCredentialsAuthentication) SecurityContextHolder.getContext().getAuthentication();
		final String accessToken = authentication.getCredentials().getAccessToken().getTokenValue();
		final StringJoiner messageStringJoiner = new StringJoiner(";");
		final String datasetName = results.get(0).getDataset();
		final String message = messageStringJoiner.toString();
		final String regionId = fetcherService.fetchDataset(ATTRIBUTE_PROJECT_ID, datasetName).getLocation();
		Publisher publisher = null;
		try {
			publisher = Publisher.newBuilder(TOPIC_NAME).build();

			ByteString data = ByteString.copyFromUtf8(message);
			PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
					.putAttributes(ATTRIBUTE_CMD_TYPE, "apply")
					.putAttributes(ATTRIBUTE_PROJECT_ID, GOOGLE_PROJECT_ID)
					.putAttributes(ATTRIBUTE_ACCESS_TOKEN, accessToken)
					.putAttributes(ATTRIBUTE_REGION_ID, regionId)
					.putAttributes(ATTRIBUTE_DATASET_NAME, datasetName)
					.setData(data).build();

			ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
			String messageId = messageIdFuture.get();
			LOGGER.info("Optimization published with messageId={}", messageId);
		} finally {
			if (publisher != null) {
				// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
			}
		}
	}
}