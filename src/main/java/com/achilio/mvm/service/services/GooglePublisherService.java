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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
  private static final String CMD_TYPE_APPLY = "apply";
  private static final String CMD_TYPE_WORKSPACE = "workspace";
  private static final Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  @Value("${publisher.google-project-id}")
  private String PUBLISHER_GOOGLE_PROJECT_ID = "achilio-dev";

  @Value("${publisher.google-topic-id}")
  private String PUBLISHER_GOOGLE_TOPIC_ID = "mvExecutorTopic";

  private final TopicName TOPIC_NAME =
      TopicName.of(PUBLISHER_GOOGLE_PROJECT_ID, PUBLISHER_GOOGLE_TOPIC_ID);

  @Value("${publisher.enabled}")
  private boolean PUBLISHER_ENABLED = true;

  public void publishOptimization(Optimization o, List<OptimizationResult> materializedViews) {
    final String projectId = o.getProjectId();
    if (!PUBLISHER_ENABLED) {
      LOGGER.info("Publisher disabled. The optimization {} will not be published.", o.getId());
      return;
    }
    if (materializedViews.isEmpty()) {
      LOGGER.info("No optimizations published:no Materialized Views found on {}", projectId);
      return;
    }
    publishMaterializedViews(projectId, materializedViews);
  }

  private Map<String, String> toEntry(OptimizationResult result) {
    Map<String, String> m = new HashMap<>();
    m.put("mmvName", result.getMvName());
    m.put("datasetName", result.getDatasetName());
    m.put("statement", result.getStatement());
    return m;
  }

  private void publishMaterializedViews(String projectId, List<OptimizationResult> mViews) {
    if (mViews.isEmpty()) {
      LOGGER.info("Empty results for the project {}: publishing skipped", projectId);
      return;
    }
    try {
      String formattedMessage = buildMaterializedViewsMessage(mViews);
      publishMessage(buildPubsubMessage(projectId, formattedMessage, CMD_TYPE_APPLY, true));
      LOGGER.info("{} results published for the project {}", mViews.size(), projectId);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error during results JSON formatting", e);
    } catch (IOException | ExecutionException | InterruptedException e) {
      LOGGER.error("Results publishing failed for the project {}", projectId, e);
    }
  }

  public String buildMaterializedViewsMessage(List<OptimizationResult> mViews)
      throws JsonProcessingException {
    List<Map<String, String>> entries =
        mViews.stream()
            .filter(Objects::nonNull)
            .filter(result -> StringUtils.isNotEmpty(result.getStatement()))
            .map(this::toEntry)
            .collect(Collectors.toList());
    return new ObjectMapper().writeValueAsString(entries);
  }

  public void publishProjectActivation(String projectId)
      throws IOException, ExecutionException, InterruptedException {
    String message = new ObjectMapper().writeValueAsString(Collections.singletonList("a"));
    publishMessage(buildPubsubMessage(projectId, message, CMD_TYPE_WORKSPACE, false));
  }

  public PubsubMessage buildPubsubMessage(
      String projectId, String message, String cmdType, boolean requireAccessToken) {
    PubsubMessage.Builder builder =
        PubsubMessage.newBuilder()
            .putAttributes(ATTRIBUTE_CMD_TYPE, cmdType)
            .putAttributes(ATTRIBUTE_PROJECT_ID, projectId);
    if (requireAccessToken) {
      builder.putAttributes(ATTRIBUTE_ACCESS_TOKEN, getAccessToken());
    }
    if (StringUtils.isNotEmpty(message)) {
      ByteString data = ByteString.copyFromUtf8(message);
      builder.setData(data);
    }
    return builder.build();
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

  private String getAccessToken() {
    return ((SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication())
        .getCredentials()
        .getAccessToken()
        .getTokenValue();
  }
}
