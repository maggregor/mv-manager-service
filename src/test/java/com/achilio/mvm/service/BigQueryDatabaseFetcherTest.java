package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableField;
import com.google.cloud.bigquery.BigQuery.TableOption;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.MaterializedViewDefinition;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
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
  private JobStatus status;
  private Job mockJob;
  private BigQuery mockBigquery;
  private ResourceManager mockResourceManager;

  @Before
  public void setUp() {
    mockBigquery = mock(BigQuery.class);
    mockResourceManager = mock(ResourceManager.class);
    fetcher = new BigQueryDatabaseFetcher(mockBigquery, mockResourceManager);
    initializeJobMockDefault();
  }

  @BeforeEach
  public void beforeEach() {
    initializeJobMockDefault();
  }

  @Test
  public void isValidTable() {
    assertFalse(fetcher.isValidTable(null));
    Table mockTable = mock(Table.class);
    StandardTableDefinition mockStandardDefinition = mock(StandardTableDefinition.class);
    MaterializedViewDefinition mockMVDefinition = mock(MaterializedViewDefinition.class);
    when(mockTable.getDefinition()).thenReturn(mockStandardDefinition);
    Schema schema = Schema.of();
    when(mockStandardDefinition.getSchema()).thenReturn(schema);
    // Is table exists
    when(mockTable.exists()).thenReturn(true);
    assertTrue(fetcher.isValidTable(mockTable));
    // Is a Materialized View
    when(mockTable.getDefinition()).thenReturn(mockMVDefinition);
    assertFalse(fetcher.isValidTable(mockTable));
    when(mockTable.getDefinition()).thenReturn(mockStandardDefinition);
    // Table doesn't exists
    when(mockTable.exists()).thenReturn(false);
    assertFalse(fetcher.isValidTable(mockTable));
    when(mockTable.exists()).thenReturn(true);
    // Table contains allowed type field
    Field field = Field.of("col1", LegacySQLTypeName.STRING);
    when(mockStandardDefinition.getSchema()).thenReturn(Schema.of(field));
    assertTrue(fetcher.isValidTable(mockTable));
    // Table contains refused type field
    Field subfield = Field.of("subfield1", LegacySQLTypeName.STRING);
    field = Field.of("col1", LegacySQLTypeName.RECORD, subfield);
    when(mockStandardDefinition.getSchema()).thenReturn(Schema.of(field));
    assertFalse(fetcher.isValidTable(mockTable));
  }

  @Test
  public void fetchTablesInDataset() {
    final String PROJECT = "myProject";
    final String DATASET = "myDataset";
    final String TABLE = "myTable";
    Page<Table> tables = mock(Page.class);
    Table table = mock(Table.class);
    TableDefinition definition = mock(StandardTableDefinition.class);
    TableId tableId = TableId.of(PROJECT, DATASET, TABLE);
    when(table.getTableId()).thenReturn(tableId);
    when(table.exists()).thenReturn(true);
    when(tables.getValues()).thenReturn(Lists.newArrayList(table));
    when(mockBigquery.listTables(DATASET)).thenReturn(tables);
    when(table.getDefinition()).thenReturn(definition);
    when(definition.getSchema()).thenReturn(Schema.of());
    when(mockBigquery.getTable(tableId, TableOption.fields(TableField.SCHEMA))).thenReturn(table);
    FetchedTable fetchedTable = fetcher.fetchTablesInDataset(DATASET).iterator().next();
    assertEquals(PROJECT, fetchedTable.getProjectId());
    assertEquals(DATASET, fetchedTable.getDatasetName());
    assertEquals(TABLE, fetchedTable.getTableName());
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

  private void initializeJobMockDefault() {
    status = mock(JobStatus.class);
    mockJob = mock(Job.class);
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    when(mockJob.getStatus()).thenReturn(status);
    when(mockJob.getStatus().getError()).thenReturn(null);
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
