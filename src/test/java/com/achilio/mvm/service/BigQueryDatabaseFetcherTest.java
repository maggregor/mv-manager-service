package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryDatabaseFetcherTest {

  private static final String defaultDatasetName = "defaultDataset";
  private static final QueryJobConfiguration DEFAULT_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.newBuilder("SELECT * FROM toto")
          .setDefaultDataset(defaultDatasetName)
          .build();
  private final Dataset mockedDataset =
      mockedDataset("myProject", "myDataset", "myDatasetFriendly", "FromParis", 100L, 1000L);
  private BigQueryDatabaseFetcher fetcher;
  private BigQuery mockBigquery;
  private ResourceManager mockResourceManager;

  @Before
  public void setUp() {
    mockBigquery = mock(BigQuery.class);
    mockResourceManager = mock(ResourceManager.class);
    fetcher = new BigQueryDatabaseFetcher(mockBigquery, mockResourceManager);
  }

  @Test
  public void fetchDataset() {
    when(mockBigquery.getDataset(any(String.class))).thenReturn(mockedDataset);
    FetchedDataset fetchedDataset = fetcher.fetchDataset("myRandomDataset");
    assertFetchedDatasetHaveTheGoodFields(mockedDataset, fetchedDataset);
  }

  @Test
  public void toFetchedProject() {
    Project project = mock(Project.class);
    when(project.getProjectId()).thenReturn("myProjectId");
    when(project.getName()).thenReturn("myProjectName");
    FetchedProject fetchedProject = fetcher.toFetchedProject(project);
    assertEquals("myProjectId", fetchedProject.getProjectId());
    assertEquals("myProjectName", fetchedProject.getName());
  }

  @Test
  public void toFetchedDataset() {
    FetchedDataset fetchedDataset = fetcher.toFetchedDataset(mockedDataset);
    assertFetchedDatasetHaveTheGoodFields(mockedDataset, fetchedDataset);
  }

  private void assertFetchedDatasetHaveTheGoodFields(Dataset expected, FetchedDataset actual) {
    assertEquals(expected.getDatasetId().getProject(), actual.getProjectId());
    assertEquals(expected.getDatasetId().getDataset(), actual.getDatasetName());
    assertEquals(expected.getFriendlyName(), actual.getFriendlyName());
    assertEquals(expected.getLocation(), actual.getLocation());
    assertEquals(expected.getCreationTime(), actual.getCreatedAt());
    assertEquals(expected.getLastModified(), actual.getLastModified());
  }

  private Dataset mockedDataset(
      String projectId,
      String datasetName,
      String friendlyName,
      String location,
      long creationTime,
      Long lastModified) {
    Dataset dataset = mock(Dataset.class);
    when(dataset.getDatasetId()).thenReturn(DatasetId.of(projectId, datasetName));
    when(dataset.getFriendlyName()).thenReturn(friendlyName);
    when(dataset.getLocation()).thenReturn(location);
    when(dataset.getCreationTime()).thenReturn(creationTime);
    when(dataset.getLastModified()).thenReturn(lastModified);
    return dataset;
  }
}
