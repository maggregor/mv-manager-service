package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.services.GoogleCloudStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/** This is an integration tests for the GoogleCloudStorage class */
@RunWith(SpringRunner.class)
@SpringBootTest
public class IntegrationGCSTest {

  private static final String PROJECT_ID = "achilio-test";
  private static final String OBJECT_PREFIX = "connections/";
  private static final String OBJECT_NAME = "achilio.com/1.json";
  private static final String CONNECTION_CONTENT = "{\"service_account\":\"content\"}";
  private static Storage storageClient;
  private static String BUCKET_NAME;

  private static GoogleCloudStorageService googleCloudStorageService;

  @BeforeClass
  public static void setupBucket() {
    BUCKET_NAME = RemoteStorageHelper.generateBucketName();
    googleCloudStorageService = new GoogleCloudStorageService(PROJECT_ID, BUCKET_NAME);
    storageClient = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storageClient.create(BucketInfo.of(BUCKET_NAME));
  }

  @AfterClass
  public static void cleanupBucket() throws ExecutionException, InterruptedException {
    RemoteStorageHelper.forceDelete(storageClient, BUCKET_NAME, 5, TimeUnit.SECONDS);
  }

  @Before
  public void setup() throws IOException {
    BlobId blobId = BlobId.of(BUCKET_NAME, "connections/dummyObject");
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    byte[] content = CONNECTION_CONTENT.getBytes(StandardCharsets.UTF_8);
    storageClient.createFrom(blobInfo, new ByteArrayInputStream(content));
  }

  @Test
  public void uploadObject() throws IOException {
    String objectUrl = googleCloudStorageService.uploadObject(OBJECT_NAME, CONNECTION_CONTENT);
    assertEquals("gcs://" + BUCKET_NAME + "/connections/" + OBJECT_NAME, objectUrl);
    Blob object = storageClient.get(BUCKET_NAME, OBJECT_NAME);
    assertNull(object);
    object = storageClient.get(BUCKET_NAME, OBJECT_PREFIX + OBJECT_NAME);
    assertEquals(CONNECTION_CONTENT, new String(object.getContent()));
  }

  @Test
  public void readObject() {
    assertEquals(CONNECTION_CONTENT, googleCloudStorageService.readObject("dummyObject"));
  }

  @Test
  public void deleteObject() {
    assertTrue(googleCloudStorageService.deleteObject("dummyObject"));
    assertFalse(googleCloudStorageService.deleteObject("dummyObject"));
  }
}
