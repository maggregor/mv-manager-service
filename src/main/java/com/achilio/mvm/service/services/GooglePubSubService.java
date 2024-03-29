package com.achilio.mvm.service.services;

import com.achilio.mvm.service.events.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GooglePubSubService implements PublisherService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePubSubService.class);

  private static final String ATTRIBUTE_TEAM_NAME = "teamName";
  private static final String ATTRIBUTE_PROJECT_ID = "projectId";
  private static final String ATTRIBUTE_EVENT_TYPE = "eventType";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final boolean publisherEnabled;
  private final TopicName sseTopicId;
  private final String emulatorHost;

  @Autowired
  public GooglePubSubService(
      @Value("${application.google-project-id}") String publisherProjectId,
      @Value("${publisher.enabled}") boolean publisherEnabled,
      @Value("${publisher.emulator.host}") String emulatorHost,
      @Value("${publisher.sse-topic-id}") String sseTopicId) {
    this.publisherEnabled = publisherEnabled;
    this.sseTopicId = TopicName.of(publisherProjectId, sseTopicId);
    this.emulatorHost = emulatorHost;
    LOGGER.info("Initialized publisher: PROJECT_ID: {}, SSE: {}", publisherProjectId,
        this.sseTopicId);
  }

  @Override
  public void handleEvent(Event event) {
    if (!publisherEnabled) {
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
          .putAttributes(ATTRIBUTE_EVENT_TYPE, event.getEventType().name())
          .setData(data).build();
      publishMessage(pubsubMessage);
    } catch (JsonProcessingException e) {
      LOGGER.error("Error while serializing event {} data to json", event.getEventType());
    } catch (InterruptedException e) {
      LOGGER.error("Error while publishing message", e);
    }
  }

  /**
   * Publish a PubsubMessage on the Google Pub/Sub
   * <p>Supports Google Pub/Sub Emulator</p>
   * To enable the communication with the Google Pub/Sub Emulator: PUBLISHER_EMULATOR_HOST must be
   * defined
   *
   * @param pubsubMessage
   * @throws InterruptedException
   */
  private void publishMessage(PubsubMessage pubsubMessage) throws InterruptedException {
    Publisher publisher = null;
    ManagedChannel channel = null;
    try {
      Publisher.Builder builder = Publisher.newBuilder(sseTopicId);
      if (StringUtils.isNotEmpty(emulatorHost)) {
        // Emulator is enabled
        channel = ManagedChannelBuilder.forTarget(emulatorHost).usePlaintext()
            .build();
        TransportChannelProvider channelProvider =
            FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
        builder.setCredentialsProvider(credentialsProvider);
        builder.setChannelProvider(channelProvider);
      }
      publisher = builder.build();
      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
        public void onSuccess(String messageId) {
          LOGGER.info("Message (id={}) published", messageId);
        }

        public void onFailure(Throwable t) {
          t.printStackTrace();
          LOGGER.error("Fail on publisher {}", t.getMessage());
        }
      }, MoreExecutors.directExecutor());
    } catch (IOException e) {
      LOGGER.error("Error while publishing message: ", e);
    } finally {
      if (publisher != null) {
        // In standard mode close the publisher
        publisher.shutdown();
        publisher.awaitTermination(1L, TimeUnit.MINUTES);
        if (channel != null) {
          // In Emulator mode close the channel
          channel.shutdown();
          channel.awaitTermination(1L, TimeUnit.MINUTES);
        }
      }
    }
  }


}
