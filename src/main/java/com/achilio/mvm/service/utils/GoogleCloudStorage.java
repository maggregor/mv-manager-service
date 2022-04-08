package com.achilio.mvm.service.utils;

import com.achilio.mvm.service.services.ConnectionService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);
  private static final String GCS_PREFIX = "gcs://";
  private static final String OBJECT_PREFIX = "connections/";

  public static String uploadObject(
      String projectId, String bucketName, String objectName, String contents) throws IOException {
    // The ID of your GCP project
    // String projectId = "your-project-id";

    // The ID of your GCS bucket
    // String bucketName = "your-unique-bucket-name";

    // The ID of your GCS object
    // String objectName = "your-object-name";

    // The path to your file to upload
    // String filePath = "path/to/your/file"

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    byte[] content = contents.getBytes(StandardCharsets.UTF_8);
    storage.createFrom(blobInfo, new ByteArrayInputStream(content));
    LOGGER.info(
        "Object "
            + objectName
            + " uploaded to bucket "
            + bucketName
            + " with contents "
            + contents);
    return GCS_PREFIX + OBJECT_PREFIX + objectName;
  }
}
