package com.achilio.mvm.service.services;

import com.achilio.mvm.service.events.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GooglePubSubService implements PublisherService {

  private static final String ATTRIBUTE_TEAM_NAME = "teamName";
  private static final String ATTRIBUTE_PROJECT_ID = "projectId";
  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePubSubService.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final boolean PUBLISHER_ENABLED;
  private final TopicName SSE_TOPIC_ID;

  @Autowired
  public GooglePubSubService(
      @Value("${publisher.enabled}") boolean publisherEnabled,
      @Value("${publisher.google-project-id}") String publisherProjectId,
      @Value("${publisher.sse-topic-id}") String sseTopicId) {
    this.PUBLISHER_ENABLED = publisherEnabled;
    this.SSE_TOPIC_ID = TopicName.of(publisherProjectId, sseTopicId);
    LOGGER.info("Initialized publisher: PROJECT_ID: {}, SSE: {}", publisherProjectId, SSE_TOPIC_ID);
  }

  @Override
  public void publishEvent(Event event) {
    if (!PUBLISHER_ENABLED) {
      LOGGER.info("Event {} not published: publisher is disabled", event.getEventType());
      return;
    }
    final String message;
    try {
      message = objectMapper.writeValueAsString(event.getData());
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
          .putAttributes(ATTRIBUTE_TEAM_NAME, event.getTeamName())
          .putAttributes(ATTRIBUTE_PROJECT_ID, event.getProjectId())
          .setData(data).build();
      publishMessage(pubsubMessage);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error while serializing event {} data to json", event.getEventType());
    }
  }

  private void publishMessage(PubsubMessage pubsubMessage) {
    Publisher publisher = null;
    try {
      publisher = Publisher.newBuilder(SSE_TOPIC_ID).build();

      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
        public void onSuccess(String messageId) {
          LOGGER.info("Message (id={}) published", messageId);
        }

        public void onFailure(Throwable t) {
          LOGGER.error("Fail on publisher {}", t.getMessage());
        }
      }, MoreExecutors.directExecutor());
    } catch (IOException e) {
      LOGGER.error("Error while publishing message: ", e);
    } finally {
      if (publisher != null) {
        publisher.shutdown();
        try {
          publisher.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

}
