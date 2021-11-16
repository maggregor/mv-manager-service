package com.achilio.mvm.service.services;

import com.achilio.mvm.service.OptimizerApplication;
import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class GooglePublisherService {

  private static final String ATTRIBUTE_CMD_TYPE = "cmdType";
  private static final String ATTRIBUTE_PROJECT_ID = "projectId";
  private static final String ATTRIBUTE_ACCESS_TOKEN = "accessToken";
  private static final String ATTRIBUTE_DATASET_NAME = "datasetName";
  private final static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  @Value("${publisher.google-project-id}")
  private String PUBLISHER_GOOGLE_PROJECT_ID = "achilio-dev";
  @Value("${publisher.google-topic-id}")
  private String PUBLISHER_GOOGLE_TOPIC_ID = "mvExecutorTopic";
  private final TopicName TOPIC_NAME = TopicName.of(PUBLISHER_GOOGLE_PROJECT_ID,
      PUBLISHER_GOOGLE_TOPIC_ID);
  @Value("${publisher.enabled}")
  private boolean PUBLISHER_ENABLED = true;

  public void publishOptimization(Optimization o, List<OptimizationResult> results) {
    if (!PUBLISHER_ENABLED) {
      LOGGER.info("Publisher disabled. The optimization {} will not be published.", o.getId());
      return;
    }
    if (results.isEmpty()) {
      LOGGER.info("No optimizations published because no results");
      return;
    }
    results.stream()
        .collect(Collectors.groupingBy(OptimizationResult::getDatasetName))
        .forEach(this::publishDatasetResults);
  }

  private void publishDatasetResults(String datasetName, List<OptimizationResult> results) {
    if (results.isEmpty()) {
      LOGGER.info("Empty results for  the dataset {}: publishing skipped", datasetName);
      return;
    }
    final String projectId = results.get(0).getProjectId();
    final String formattedMessage = getFormattedMessage(results);
    PubsubMessage pubsubMessage = buildPubsubMessage(projectId, datasetName, formattedMessage);
    try {
      publishMessage(pubsubMessage);
      LOGGER.info("{} results published for the dataset {}", results.size(), datasetName);
    } catch (IOException | ExecutionException | InterruptedException e) {
      LOGGER.error("Results publishing failed for the dataset {}", datasetName, e);
    }
  }

  public PubsubMessage buildPubsubMessage(String projectId, String datasetName, String message) {
    ByteString data = ByteString.copyFromUtf8(message);
    return PubsubMessage.newBuilder()
        .putAttributes(ATTRIBUTE_CMD_TYPE, "apply")
        .putAttributes(ATTRIBUTE_PROJECT_ID, projectId)
        .putAttributes(ATTRIBUTE_ACCESS_TOKEN, getAccessToken())
        .putAttributes(ATTRIBUTE_DATASET_NAME, datasetName)
        .setData(data)
        .build();
  }

  public void publishMessage(PubsubMessage pubsubMessage)
      throws IOException, ExecutionException, InterruptedException {
    Publisher publisher = null;
    try {
      publisher = Publisher.newBuilder(TOPIC_NAME).build();
      publisher.publish(pubsubMessage);
    } finally {
      if (publisher != null) {
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }

  public String toJSONArrayOfResultStatements(List<OptimizationResult> results)
      throws JsonProcessingException {
    return new ObjectMapper()
        .writeValueAsString(
            results.stream()
                .map(OptimizationResult::getStatement)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

  }

  public String getFormattedMessage(List<OptimizationResult> results) {
    final StringJoiner messageStringJoiner = new StringJoiner(";");
    results.stream().map(OptimizationResult::getStatement).forEach(messageStringJoiner::add);
    return messageStringJoiner.toString();
  }

  private String getAccessToken() {
    return ((SimpleGoogleCredentialsAuthentication)
        SecurityContextHolder.getContext().getAuthentication())
        .getCredentials()
        .getAccessToken()
        .getTokenValue();
  }
}
