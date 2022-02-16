package com.achilio.mvm.service.services;

import com.achilio.mvm.service.OptimizerApplication;
import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.entities.Project;
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
  private static final String CMD_TYPE_DESTROY = "destroy";
  private static final Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  @Value("${publisher.enabled}")
  private boolean PUBLISHER_ENABLED = false;

  @Value("${publisher.google-project-id}")
  private String PUBLISHER_GOOGLE_PROJECT_ID = "achilio-dev";

  @Value("${publisher.executor-topic-id}")
  private String PUBLISHER_EXECUTOR_TOPIC_ID = "mvExecutorTopic";

  private final TopicName EXECUTOR_TOPIC_NAME =
      TopicName.of(PUBLISHER_GOOGLE_PROJECT_ID, PUBLISHER_EXECUTOR_TOPIC_ID);

  @Value("${publisher.scheduler-topic-id}")
  private String PUBLISHER_SCHEDULER_TOPIC_ID = "mvScheduleManagerTopic";

  private final TopicName SCHEDULER_TOPIC_NAME =
      TopicName.of(PUBLISHER_GOOGLE_PROJECT_ID, PUBLISHER_SCHEDULER_TOPIC_ID);

  public Boolean publishOptimization(Optimization o, List<OptimizationResult> materializedViews) {
    final String projectId = o.getProjectId();
    if (materializedViews.isEmpty()) {
      LOGGER.info("No optimizations published:no Materialized Views found on {}", projectId);
      return false;
    }
    return publishMaterializedViews(projectId, materializedViews);
  }

  private Map<String, String> toProjectEntry(Project project) {
    Map<String, String> m = new HashMap<>();
    m.put("projectId", project.getProjectId());
    m.put("username", project.getUsername());
    return m;
  }

  private Map<String, String> toResultEntry(OptimizationResult result) {
    Map<String, String> m = new HashMap<>();
    m.put("mmvName", result.getMvName());
    m.put("datasetName", result.getDatasetName());
    m.put("statement", result.getStatement());
    return m;
  }

  private Boolean publishMaterializedViews(String projectId, List<OptimizationResult> mViews) {
    if (mViews.isEmpty()) {
      LOGGER.info("Empty results for the project {}: publishing skipped", projectId);
      return false;
    }
    try {
      String formattedMessage = buildMaterializedViewsMessage(mViews);
      Boolean published =
          publishMessage(
              buildMaterializedViewsPubsubMessage(
                  projectId, formattedMessage, CMD_TYPE_APPLY, true),
              EXECUTOR_TOPIC_NAME);
      if (published) {
        LOGGER.info("{} results published for the project {}", mViews.size(), projectId);
        return true;
      }
    } catch (JsonProcessingException e) {
      LOGGER.error("Error during results JSON formatting", e);
      return false;
    } catch (IOException | ExecutionException | InterruptedException e) {
      LOGGER.error("Results publishing failed for the project {}", projectId, e);
      return false;
    }
    return false;
  }

  public String buildMaterializedViewsMessage(List<OptimizationResult> mViews)
      throws JsonProcessingException {
    List<Map<String, String>> entries =
        mViews.stream()
            .filter(Objects::nonNull)
            .filter(result -> StringUtils.isNotEmpty(result.getStatement()))
            .map(this::toResultEntry)
            .collect(Collectors.toList());
    return new ObjectMapper().writeValueAsString(entries);
  }

  public void publishDestroyMaterializedViews(String projectId) {
    try {
      String message =
          new ObjectMapper()
              .writeValueAsString(
                  Collections.singletonList("Destroying all MMVs in project " + projectId));
      publishMessage(
          buildMaterializedViewsPubsubMessage(projectId, message, CMD_TYPE_DESTROY, true),
          EXECUTOR_TOPIC_NAME);
      LOGGER.info("All MMVs destroyed for the project {}", projectId);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error during results JSON formatting", e);
    } catch (IOException | ExecutionException | InterruptedException e) {
      LOGGER.error("Results publishing failed for the project {}", projectId, e);
    }
  }

  public void publishProjectSchedulers(List<Project> projects) {
    try {
      String formattedMessage = buildSchedulerMessage(projects);
      if (publishMessage(
          buildSchedulerPubSubMessage(CMD_TYPE_APPLY, formattedMessage), SCHEDULER_TOPIC_NAME)) {
        LOGGER.info("Published update of all projects schedulers");
      } else {
        LOGGER.info(
            "Project has been updated in the database. But pubsub has not been sent and Cloud Schedulers will not update");
      }
    } catch (JsonProcessingException e) {
      LOGGER.error("Error during results JSON formatting", e);
    } catch (IOException | ExecutionException | InterruptedException e) {
      LOGGER.error("Automatic scheduler publishing failed", e);
    }
  }

  public void publishProjectActivation(String projectId)
      throws IOException, ExecutionException, InterruptedException {
    String message =
        new ObjectMapper()
            .writeValueAsString(Collections.singletonList("Activating project " + projectId));
    if (publishMessage(
        buildMaterializedViewsPubsubMessage(projectId, message, CMD_TYPE_WORKSPACE, false),
        EXECUTOR_TOPIC_NAME)) {
      LOGGER.info("Activating project {}", projectId);
    } else {
      LOGGER.info(
          "Project {} is activated in database. But pubsub has not been sent and Workspace may not be created",
          projectId);
    }
  }

  public String buildSchedulerMessage(List<Project> projects) throws JsonProcessingException {
    List<Map<String, String>> entries =
        projects.stream()
            .filter(Objects::nonNull)
            .filter(project -> StringUtils.isNotEmpty(project.getUsername()))
            .filter(Project::isAutomatic) // Should already be the case but just for safety
            .map(this::toProjectEntry)
            .collect(Collectors.toList());
    return new ObjectMapper().writeValueAsString(entries);
  }

  public PubsubMessage buildSchedulerPubSubMessage(String cmdType, String message) {
    PubsubMessage.Builder builder =
        PubsubMessage.newBuilder().putAttributes(ATTRIBUTE_CMD_TYPE, cmdType);
    ByteString data = ByteString.copyFromUtf8(message);
    builder.setData(data);
    return builder.build();
  }

  public PubsubMessage buildMaterializedViewsPubsubMessage(
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

  public Boolean publishMessage(PubsubMessage pubsubMessage, TopicName topicName)
      throws IOException, ExecutionException, InterruptedException {
    Publisher publisher = null;
    if (!PUBLISHER_ENABLED) {
      LOGGER.info(
          "Publisher disabled. Message {} with data {} will not be published.",
          pubsubMessage.getMessageId(),
          pubsubMessage.getData());
      return false;
    }
    try {
      publisher = Publisher.newBuilder(topicName).build();
      publisher.publish(pubsubMessage);
    } finally {
      if (publisher != null) {
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
    return true;
  }

  private String getAccessToken() {
    return ((SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication())
        .getCredentials()
        .getAccessToken()
        .getTokenValue();
  }
}
