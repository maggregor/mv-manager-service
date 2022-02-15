package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.google.api.gax.paging.Page;
import com.google.api.gax.paging.Pages;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableField;
import com.google.cloud.bigquery.BigQuery.TableOption;
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
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.ResourceManager;
import java.util.ArrayList;
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
public class BigQueryDatabaseFetcherTest {

  private static final QueryJobConfiguration DEFAULT_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.of("SELECT * FROM toto");
  private static final QueryJobConfiguration COUNT_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.of("SELECT COUNT(*) FROM toto");
  private static final QueryJobConfiguration SUM_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.of("SELECT SUM(a) FROM toto GROUP BY a");
  private static final QueryJobConfiguration INFO_QUERY_JOB_CONFIGURATION =
      QueryJobConfiguration.of("SELECT a FROM INFORMATION_SCHEMA");
  private static final TableId DEFAULT_TABLE_ID =
      TableId.of("test-project", "test-dataset", "test-table");
  private static final TableId DEFAULT_TABLE_ID_2 =
      TableId.of("test-project", "test-dataset", "test-table-2");
  private static final JobConfiguration DEFAULT_LOAD_JOB_CONFIGURATION =
      LoadJobConfiguration.newBuilder(DEFAULT_TABLE_ID, "gs://").build();
  private static final JobConfiguration DEFAULT_COPY_JOB_CONFIGURATION =
      CopyJobConfiguration.newBuilder(DEFAULT_TABLE_ID, DEFAULT_TABLE_ID_2).build();
  private BigQueryDatabaseFetcher fetcher;
  private JobStatus status;
  private Job mockJob;
  private Page<Job> jobs;
  private JobStatistics.QueryStatistics mockJobStats;
  private BigQuery mockBigquery;

  @Before
  public void setUp() {
    mockBigquery = mock(BigQuery.class);
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
  public void fetchingRefuseErrorJob() {
    when(mockJob.getStatus().getError()).thenReturn(new BigQueryError("Im", "in", "error"));
    assertDoNotPassTheFetchingFilter(mockJob);
  }

  @Test
  public void fetchingMetadataSelectQuery() {
    when(mockJob.getConfiguration())
        .thenReturn(QueryJobConfiguration.of("SELECT a FROM INFORMATION_SCHEMA"));
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
    when(mockJob.getConfiguration())
        .thenReturn(
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
  public void isSQLScript() {
    assertFalse(fetcher.isSQLScript(Strings.EMPTY));
    assertTrue(fetcher.isSQLScript("SELECT 1; SELECT 2;"));
    assertTrue(fetcher.isSQLScript("SELECT COUNT(*) FROM myTable; SELECT 2"));
    assertFalse(fetcher.isSQLScript("SELECT COUNT(*) FROM myTable"));
    assertFalse(fetcher.isSQLScript(" SELECT 1; ")); // with spaces
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
  public void isRegularSelectQuery() {
    assertFalse(fetcher.isRegularSelectQuery("SELECT 1"));
    assertFalse(fetcher.isRegularSelectQuery("CALL.BQ...."));
    assertFalse(fetcher.isRegularSelectQuery("SELECT * FROM INFORMATION_SCHEMA"));
    assertTrue(fetcher.isRegularSelectQuery("SELECT COUNT(*) FROM myTable"));
    assertTrue(fetcher.isRegularSelectQuery(" SELECT COUNT(*) FROM myTable"));
    assertTrue(fetcher.isRegularSelectQuery("\nSELECT COUNT(*) FROM myTable"));
    assertTrue(fetcher.isRegularSelectQuery("\rSELECT COUNT(*) FROM myTable"));
  }

  @Test
  public void queryContainsComments() {
    final String REGULAR_SUPERSET_QUERY =
        "-- 6dcd92a04feb50f14bbcf07c661680ba\n"
            + "SELECT TIMESTAMP_TRUNC(`pickup_datetime`, WEEK) AS `__timestamp`,\n"
            + "       `passenger_count` AS `passenger_count`,\n"
            + "       sum(`tip_amount`) AS `SUM_tip_amount__f2041`\n"
            + "FROM `nyc_trips`.`tlc_yellow_trips_2015_small`\n"
            + "WHERE `payment_type` IN ('2',\n"
            + "                         '3')\n"
            + "  AND `passenger_count` IN (2,\n"
            + "                            4,\n"
            + "                            0,\n"
            + "                            6,\n"
            + "                            1)\n"
            + "  AND `rate_code` <= 41\n"
            + "GROUP BY `passenger_count`,\n"
            + "         `__timestamp`\n"
            + "ORDER BY SUM_tip_amount__f2041 DESC\n"
            + "LIMIT 10000\n"
            + "-- 6dcd92a04feb50f14bbcf07c661680ba";
    assertTrue(fetcher.isRegularSelectQuery(REGULAR_SUPERSET_QUERY));
  }

  @Test
  public void jobStatisticsToQueryStatistics() {
    final QueryStatistics expected = new QueryStatistics();
    expected.addBilledBytes(100L);
    expected.addProcessedBytes(10L);
    JobStatistics.QueryStatistics mockJobStats = mock(JobStatistics.QueryStatistics.class);
    when(mockJobStats.getTotalBytesBilled()).thenReturn(100L);
    when(mockJobStats.getTotalBytesProcessed()).thenReturn(10L);
    QueryUsageStatistics actual = fetcher.toQueryUsageStatistics(mockJobStats);
    assertNotNull(actual);
    assertEquals(expected.getTotalBilledBytes(), actual.getBilledBytes());
    assertEquals(expected.getTotalProcessedBytes(), actual.getProcessedBytes());
  }

  @Test
  public void containsSubStepUsingMVM() {
    QueryStep step = mock(QueryStep.class);
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "sub2", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM myTable", "sub3"));
    assertFalse(fetcher.containsSubStepUsingMVM(step));
    when(step.getSubsteps()).thenReturn(createSubSteps("sub1", "FROM mvm_", "sub3"));
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
    when(steps2.getSubsteps()).thenReturn(createSubSteps("FROM mvm_", "st2"));
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
    when(mockTable.exists()).thenReturn(true);
    assertTrue(fetcher.isValidTable(mockTable));
    when(mockTable.getDefinition()).thenReturn(mockMVDefinition);
    assertFalse(fetcher.isValidTable(mockTable));
    when(mockTable.exists()).thenReturn(false);
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
    when(mockBigquery.getTable(tableId, TableOption.fields(TableField.SCHEMA))).thenReturn(table);
    FetchedTable fetchedTable = fetcher.fetchTablesInDataset(DATASET).iterator().next();
    assertEquals(PROJECT, fetchedTable.getProjectId());
    assertEquals(DATASET, fetchedTable.getDatasetName());
    assertEquals(TABLE, fetchedTable.getTableName());
  }

  @Test
  public void fetchNoQuery() {
    Page<Job> jobs = Pages.empty();
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(BigQueryDatabaseFetcher.LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(0));
    when(mockBigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]))).thenReturn(jobs);
    List<FetchedQuery> queries = fetcher.fetchAllQueries();
    assertEmptyQueries(queries);
  }

  @Test
  public void fetchOneQuery() {
    QueryStage stage1 = mock(QueryStage.class);
    QueryStep steps1 = mock(QueryStep.class);
    when(stage1.getSteps()).thenReturn(Lists.newArrayList(steps1));
    when(steps1.getSubsteps()).thenReturn(createSubSteps("st1", "st2"));
    when(mockJobStats.getQueryPlan()).thenReturn(Lists.newArrayList(stage1));
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(BigQueryDatabaseFetcher.LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(0));
    when(mockBigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]))).thenReturn(jobs);
    List<FetchedQuery> queries = fetcher.fetchAllQueries();
    assertListSize(1, queries);
  }

  @Test
  public void fetchTwoQueries() {
    QueryStage stage1 = mock(QueryStage.class);
    QueryStep steps1 = mock(QueryStep.class);
    Job mockJob1 = mock(Job.class);
    when(mockJob1.getConfiguration()).thenReturn(COUNT_QUERY_JOB_CONFIGURATION);
    when(mockJob1.getStatus()).thenReturn(status);
    when(mockJob1.getStatus().getError()).thenReturn(null);
    when(mockJob1.getStatistics()).thenReturn(mockJobStats);
    when(stage1.getSteps()).thenReturn(Lists.newArrayList(steps1));
    when(steps1.getSubsteps()).thenReturn(createSubSteps("st1", "st2"));
    when(mockJobStats.getQueryPlan()).thenReturn(Lists.newArrayList(stage1));
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(BigQueryDatabaseFetcher.LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(0));
    when(jobs.iterateAll()).thenReturn(Lists.newArrayList(mockJob, mockJob1));
    when(mockBigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]))).thenReturn(jobs);
    List<FetchedQuery> queries = fetcher.fetchAllQueries();
    assertListSize(2, queries);
    when(mockJob1.getConfiguration()).thenReturn(INFO_QUERY_JOB_CONFIGURATION);
    queries = fetcher.fetchAllQueries();
    assertListSize(1, queries);
  }

  private void assertListSize(long listSize, List<FetchedQuery> queries) {
    assertEquals(listSize, queries.size());
  }

  private void assertEmptyQueries(List<FetchedQuery> queries) {
    assertTrue(queries.isEmpty());
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
    status = mock(JobStatus.class);
    mockJobStats = mock(JobStatistics.QueryStatistics.class);
    mockJob = mock(Job.class);
    when(mockJob.getConfiguration()).thenReturn(DEFAULT_QUERY_JOB_CONFIGURATION);
    when(mockJob.getStatus()).thenReturn(status);
    when(mockJob.getStatus().getError()).thenReturn(null);
    when(mockJob.getStatistics()).thenReturn(mockJobStats);
    when(mockJobStats.getCacheHit()).thenReturn(false);
    jobs = mock(Page.class);
    when(jobs.iterateAll()).thenReturn(Lists.newArrayList(mockJob));
  }
}
