package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.CopyJobConfiguration;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobConfiguration;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.MaterializedViewDefinition;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.ResourceManager;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryDatabaseFetchedTest {

  private static final QueryJobConfiguration DEFAULT_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.of("SELECT * FROM toto");
  private static final TableId DEFAULT_TABLE_ID = TableId.of("test-project", "test-dataset",
      "test-table");
  private static final TableId DEFAULT_TABLE_ID_2 = TableId.of("test-project", "test-dataset",
      "test-table-2");
  private static final JobConfiguration DEFAULT_LOAD_JOB_CONFIGURATION = LoadJobConfiguration.newBuilder(
      DEFAULT_TABLE_ID, "gs://").build();
  private static final JobConfiguration DEFAULT_COPY_JOB_CONFIGURATION = CopyJobConfiguration.newBuilder(
      DEFAULT_TABLE_ID, DEFAULT_TABLE_ID_2).build();
  private BigQueryDatabaseFetcher fetcher;
  private Job mockJob;

  @Before
  public void setUp() {
    BigQuery mockBigquery = mock(BigQuery.class);
    ResourceManager resourceManager = mock(ResourceManager.class);
    fetcher = new BigQueryDatabaseFetcher(mockBigquery, resourceManager, "test-project");
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
  public void fetchingRefusePendingJob() {
    when(mockJob.isDone()).thenReturn(false);
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingPassCompletedJob() {
    when(mockJob.isDone()).thenReturn(true);
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseErrorJob() {
    when(mockJob.getStatus().getError()).thenReturn(new BigQueryError("Im", "in", "error"));
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingMetadataSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(
        QueryJobConfiguration.of("SELECT a FROM INFORMATION_SCHEMA"));
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseNonSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(QueryJobConfiguration.of("CALL.BQ myTest"));
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingPassSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(QueryJobConfiguration.of("SELECT a FROM myTest"));
    assertPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingRefuseScriptSelectQuery() {
    when(mockJob.getConfiguration()).thenReturn(
        QueryJobConfiguration.of("SELECT * FROM myTest; SELECT COUNT(*) FROM besancon"));
    assertDoNotPassTheFetchingFilter(mockJob);
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
  public void testSQLScript() {
    assertFalse(fetcher.isSQLScript(Strings.EMPTY));
    assertTrue(fetcher.isSQLScript("SELECT 1; SELECT 2;"));
    assertTrue(fetcher.isSQLScript("SELECT COUNT(*) FROM myTable; SELECT 2"));
    assertFalse(fetcher.isSQLScript("SELECT COUNT(*) FROM myTable"));
    assertFalse(fetcher.isSQLScript(" SELECT 1; ")); // with spaces
  }

  @Test
  public void testIsQueryJob() {
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_LOAD_JOB_CONFIGURATION);
    assertFalse(fetcher.isQueryJob(mockJob));
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_COPY_JOB_CONFIGURATION);
    assertFalse(fetcher.isQueryJob(mockJob));
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    assertTrue(fetcher.isQueryJob(mockJob));
  }

  @Test
  public void testNotInError() {
    when(mockJob.getStatus().getError()).thenReturn(new BigQueryError("a", "b", "c"));
    assertFalse(fetcher.notInError(mockJob));
    when(mockJob.getStatus().getError()).thenReturn(null);
    assertTrue(fetcher.notInError(mockJob));
  }

  @Test
  public void testIsRegularSelectQuery() {
    assertFalse(fetcher.isRegularSelectQuery("SELECT 1"));
    assertFalse(fetcher.isRegularSelectQuery("CALL.BQ...."));
    assertFalse(fetcher.isRegularSelectQuery("SELECT * FROM INFORMATION_SCHEMA"));
    assertTrue(fetcher.isRegularSelectQuery("SELECT COUNT(*) FROM myTable"));
  }

  @Test
  public void testJobStatisticsToQueryStatistics() {
    final QueryStatistics expected = new QueryStatistics();
    expected.addBilledBytes(100L);
    expected.addProcessedBytes(10L);
    JobStatistics.QueryStatistics mockJobStats = mock(JobStatistics.QueryStatistics.class);
    when(mockJobStats.getTotalBytesBilled()).thenReturn(100L);
    when(mockJobStats.getTotalBytesProcessed()).thenReturn(10L);
    QueryStatistics actual = fetcher.toQueryStatistics(mockJobStats);
    assertNotNull(actual);
    assertEquals(expected.getBilledBytes(), actual.getBilledBytes());
    assertEquals(expected.getProcessedBytes(), actual.getProcessedBytes());
  }

  @Test
  public void testContainsSubStepUsingMVM() {
    QueryStep step = mock(QueryStep.class);
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "sub2", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM myTable", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM mvm_", "sub3"));
    assertTrue(fetcher.containsSubStepUsingMVM(step));
  }

  @Test
  public void testContainsManagedMVUsageInQueryStages() {
    assertFalse(fetcher.containsManagedMVUsageInQueryStages(null));
    QueryStage stage1 = mock(QueryStage.class);
    QueryStage stage2 = mock(QueryStage.class);
    QueryStep steps1 = mock(QueryStep.class);
    QueryStep steps2 = mock(QueryStep.class);
    when(stage1.getSteps()).thenReturn(Lists.newArrayList(steps1));
    when(stage2.getSteps()).thenReturn(Lists.newArrayList(steps2));
    when(steps1.getSubsteps()).thenReturn(createSubSteps("st1", "st2"));
    when(steps2.getSubsteps()).thenReturn(createSubSteps("FROM mvm_", "st2"));
    assertFalse(fetcher.containsManagedMVUsageInQueryStages(Lists.newArrayList(stage1)));
    assertTrue(fetcher.containsManagedMVUsageInQueryStages(Lists.newArrayList(stage2)));
  }

  @Test
  public void testIsValidTable() {
    assertFalse(fetcher.isValidTable(null));
    Table mockTable = mock(Table.class);
    StandardTableDefinition mockStandardDefinition = mock(StandardTableDefinition.class);
    MaterializedViewDefinition mockMVDefinition = mock(MaterializedViewDefinition.class);
    when(mockTable.getDefinition()).thenReturn(mockStandardDefinition);
    when(mockTable.exists()).thenReturn(true);
    assertTrue(fetcher.isValidTable(mockTable));
    when(mockTable.getDefinition()).thenReturn(mockMVDefinition);
    assertFalse(fetcher.isValidTable(mockTable));
  }

  private void assertPassTheFetchingFilter(Job job) {
    assertTrue("This job does not pass the filter", fetcher.fetchQueryFilter(job));
  }

  private void assertDoNotPassTheFetchingFilter(Job job) {
    assertFalse("This job does pass the filter", fetcher.fetchQueryFilter(job));
  }

  private List<String> createSubSteps(String... subSteps) {
    return Arrays.asList(subSteps);
  }

  private void initializeJobMockDefault() {
    JobStatus status = mock(JobStatus.class);
    mockJob = mock(Job.class);
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    when(mockJob.getStatus()).thenReturn(status);
    when(mockJob.isDone()).thenReturn(true);
  }

}