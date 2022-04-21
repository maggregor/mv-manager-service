package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryDatabaseFetcherTest {

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
  public void toFetchedProject() {
    Project project = mock(Project.class);
    when(project.getProjectId()).thenReturn("myProjectId");
    when(project.getName()).thenReturn("myProjectName");
    FetchedProject fetchedProject = fetcher.toFetchedProject(project);
    assertEquals("myProjectId", fetchedProject.getProjectId());
    assertEquals("myProjectName", fetchedProject.getName());
  }
}
