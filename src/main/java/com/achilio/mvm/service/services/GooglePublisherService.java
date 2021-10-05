package com.achilio.mvm.service.services;

import com.achilio.mvm.service.OptimizerApplication;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${publisher.google-project-id}")
	private String PUBLISHER_GOOGLE_PROJECT_ID = "achilio-dev";
	@Value("${publisher.google-topic-id}")
	private String PUBLISHER_GOOGLE_TOPIC_ID = "mvExecutorTopic";
	private final static String ATTRIBUTE_CMD_TYPE = "cmdType";
	private final static String ATTRIBUTE_PROJECT_ID = "projectId";
	private final static String ATTRIBUTE_ACCESS_TOKEN = "accessToken";
	private final static String ATTRIBUTE_REGION_ID = "regionId";
	private final static String ATTRIBUTE_DATASET_NAME = "datasetName";

	private final TopicName TOPIC_NAME = TopicName.of(PUBLISHER_GOOGLE_PROJECT_ID, PUBLISHER_GOOGLE_TOPIC_ID);

	public void publishOptimization(Optimization optimization,
									List<OptimizationResult> results,
									String accessToken
	) throws IOException, ExecutionException, InterruptedException {
		if (results.isEmpty()) {
			LOGGER.info("No optimizations published because no results.");
			return;
		}
		final StringJoiner messageStringJoiner = new StringJoiner(";");
		results.stream()
				.map(OptimizationResult::getStatement)
				.forEach(messageStringJoiner::add);
		final String message = messageStringJoiner.toString();
		final String projectId = optimization.getProjectId();
		final String datasetName = optimization.getDatasetName();
		final String regionId = optimization.getRegionId();
		Publisher publisher = null;
		try {
			publisher = Publisher.newBuilder(TOPIC_NAME).build();

			ByteString data = ByteString.copyFromUtf8(message);
			PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
					.putAttributes(ATTRIBUTE_CMD_TYPE, "apply")
					.putAttributes(ATTRIBUTE_PROJECT_ID, projectId)
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
