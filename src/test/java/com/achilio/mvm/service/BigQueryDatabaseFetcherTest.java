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
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.CopyJobConfiguration;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobConfiguration;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.MaterializedViewDefinition;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryDatabaseFetcherTest {

  private static final String defaultDatasetName = "defaultDataset";
  private static final TableId DEFAULT_TABLE_ID =
      TableId.of("test-project", "test-dataset", "test-table");
  private static final TableId DEFAULT_TABLE_ID_2 =
      TableId.of("test-project", "test-dataset", "test-table-2");
  private static final JobConfiguration DEFAULT_LOAD_JOB_CONFIGURATION =
      LoadJobConfiguration.newBuilder(DEFAULT_TABLE_ID, "gs://").build();
  private static final JobConfiguration DEFAULT_COPY_JOB_CONFIGURATION =
      CopyJobConfiguration.newBuilder(DEFAULT_TABLE_ID, DEFAULT_TABLE_ID_2).build();
  private static final QueryJobConfiguration DEFAULT_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.newBuilder("SELECT * FROM toto")
          .setDefaultDataset(defaultDatasetName)
          .build();
  private final Dataset mockedDataset =
      mockedDataset("myProject", "myDataset", "myDatasetFriendly", "FromParis", 100L, 1000L);
  private final String googleJobId1 = "google-id1";
  private final String projectId1 = "myProjectId1";
  private BigQueryDatabaseFetcher fetcher;
  private JobStatus status;
  private Page<Job> jobs;
  private Job mockJob;
  private JobStatistics.QueryStatistics mockJobStats;
  private BigQuery mockBigquery;
  private ResourceManager mockResourceManager;
  private JobId mockJobId;

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
  public void fetchingRefuseNullJob() {
    Job nullJob = null;
    assertDoNotPassTheFetchingFilter(nullJob);
  }

  @Test
  public void fetchingRefuseErrorJob() {
    when(mockJob.getStatus().getError()).thenReturn(new BigQueryError("Im", "in", "error"));
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseNonSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(QueryJobConfiguration.of("CALL.BQ myTest"));
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingPassSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(QueryJobConfiguration.of("SELECT a FROM myTest"));
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseScriptSelectQuery() {
    when(mockJob.getConfiguration())
        .thenReturn(
            QueryJobConfiguration.of("SELECT * FROM myTest; SELECT COUNT(*) FROM besancon"));
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseNonQueryJob() {
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_LOAD_JOB_CONFIGURATION);
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingPassNonQueryJob() {
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void isQueryJob() {
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_LOAD_JOB_CONFIGURATION);
    assertFalse(fetcher.isQueryJob(mockJob));
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_COPY_JOB_CONFIGURATION);
    assertFalse(fetcher.isQueryJob(mockJob));
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    assertTrue(fetcher.isQueryJob(mockJob));
  }

  @Test
  public void notInError() {
    when(mockJob.getStatus().getError()).thenReturn(new BigQueryError("a", "b", "c"));
    assertFalse(fetcher.notInError(mockJob));
    when(mockJob.getStatus().getError()).thenReturn(null);
    assertTrue(fetcher.notInError(mockJob));
  }

  @Test
  public void containsSubStepUsingMVM() {
    QueryStep step = mock(QueryStep.class);
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "sub2", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM myTable", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM achilio_mv_", "sub3"));
    assertTrue(fetcher.containsSubStepUsingMVM(step));
  }

  @Test
  public void containsManagedMVUsageInQueryStages() {
    assertFalse(fetcher.containsManagedMVUsageInQueryStages(null));
    QueryStage stage1 = mock(QueryStage.class);
    QueryStage stage2 = mock(QueryStage.class);
    QueryStep steps1 = mock(QueryStep.class);
    QueryStep steps2 = mock(QueryStep.class);
    when(stage1.getSteps()).thenReturn(Lists.newArrayList(steps1));
    when(stage2.getSteps()).thenReturn(Lists.newArrayList(steps2));
    when(steps1.getSubsteps()).thenReturn(createSubSteps("st1", "st2"));
    when(steps2.getSubsteps()).thenReturn(createSubSteps("FROM achilio_mv_", "st2"));
    assertFalse(fetcher.containsManagedMVUsageInQueryStages(Lists.newArrayList(stage1)));
    assertTrue(fetcher.containsManagedMVUsageInQueryStages(Lists.newArrayList(stage2)));
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
  public void fetchTable() {}

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

  private void assertPassTheFetchingFilter(Job job) {
    assertTrue("This job does not pass the filter", fetcher.isValidQueryJob(job));
  }

  private void assertDoNotPassTheFetchingFilter(Job job) {
    assertFalse("This job does pass the filter", fetcher.isValidQueryJob(job));
  }

  private List<String> createSubSteps(String... subSteps) {
    return Arrays.asList(subSteps);
  }

  private void initializeJobMockDefault() {
    status = mock(JobStatus.class);
    mockJobStats = mock(JobStatistics.QueryStatistics.class);
    mockJob = mock(Job.class);
    mockJobId = mock(JobId.class);
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    when(mockJob.getStatus()).thenReturn(status);
    when(mockJob.getStatus().getError()).thenReturn(null);
    jobs = mock(Page.class);
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
