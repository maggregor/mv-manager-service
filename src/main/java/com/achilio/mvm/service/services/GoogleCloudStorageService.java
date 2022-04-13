package com.achilio.mvm.service.services;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleCloudStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);
  private static final String GCS_PREFIX = "gcs://";
  private static final String OBJECT_PREFIX = "connections/";
  private final String BUCKET_NAME;
  private final Storage storage;

  public GoogleCloudStorageService(
      @Value("${application.google-project-id}") String projectId,
      @Value("${connection.bucket.name}") String bucketName) {
    this.BUCKET_NAME = bucketName;
    this.storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
  }

  public String uploadObject(String objectName, String contents) throws IOException {
    String objectFullName = OBJECT_PREFIX + objectName;
    BlobId blobId = BlobId.of(BUCKET_NAME, objectFullName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    byte[] content = contents.getBytes(StandardCharsets.UTF_8);
    storage.createFrom(blobInfo, new ByteArrayInputStream(content));
    LOGGER.info("Object " + objectFullName + " uploaded to bucket " + BUCKET_NAME);
    return GCS_PREFIX + BUCKET_NAME + "/" + OBJECT_PREFIX + objectName;
  }

  public String readObject(String objectName) {
    String objectFullName = OBJECT_PREFIX + objectName;
    byte[] content = storage.readAllBytes(BUCKET_NAME, objectFullName);
    return new String(content);
  }

  public Boolean deleteObject(String objectName) {
    String objectFullName = OBJECT_PREFIX + objectName;
    BlobId blobId = BlobId.of(BUCKET_NAME, objectFullName);
    return storage.delete(blobId);
  }
}
