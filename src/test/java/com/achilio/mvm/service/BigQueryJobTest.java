package com.achilio.mvm.service;

import static com.achilio.mvm.service.entities.MaterializedView.MV_NAME_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.BigQueryJob;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryJobTest {


  @Test
  public void when_JobIsNotQueryConfigurationJob__thenThrowException() {
    Job job = simpleJobMock();
    when(job.getConfiguration()).thenReturn(mock(LoadJobConfiguration.class));
    assertThrows(IllegalArgumentException.class, () -> new BigQueryJob(job));
  }

  @Test
  public void when_SomeStatisticsAreNull__thenSetStatisticsToZeroOrNull() {
    Job job = simpleJobMock();
    setStatisticsWithNullFields(job);
    AQuery query = new BigQueryJob(job);
    assertFalse(query.isUseCache());
    assertEquals(0L, query.getBilledBytes());
    assertEquals(0L, query.getProcessedBytes());
    assertNull(query.getStartTime());
  }

  @Test
  public void bigQueryJobToQuery() {
    Job job = simpleJobMock();
    AQuery query = new BigQueryJob(job);
    assertEquals("SELECT 1", query.getQuery());
    assertNull(query.getDefaultDataset());
    assertFalse(query.hasDefaultDataset());
    assertTrue(StringUtils.isEmpty(query.getError()));
    assertEquals("job-id", query.getId());
    assertEquals(1650394735L, query.getStartTime().toInstant().toEpochMilli());
    assertEquals(10000L, query.getProcessedBytes());
    assertEquals(50000L, query.getBilledBytes());
  }

  @Test
  public void bigQueryJobToQueryWithDefaultDataset() {
    Job job = simpleJobMock();
    QueryJobConfiguration configuration = mock(QueryJobConfiguration.class);
    when(configuration.getDefaultDataset()).thenReturn(DatasetId.of("myDefaultDataset"));
    when(job.getConfiguration()).thenReturn(configuration);
    AQuery query = new BigQueryJob(job);
    assertEquals("myDefaultDataset", query.getDefaultDataset());
  }

  @Test
  public void when_bigQueryJobHaveASubStepInMV__setUseMaterializedViewAsTrue() {
    Job job = simpleJobMock();
    // Build a queryPlan with MV
    List<QueryStage> queryPlan = new ArrayList<>();
    QueryStage stage_1 = mock(QueryStage.class);
    QueryStep step_1_1 = mock(QueryStep.class);
    QueryStep step_1_2 = mock(QueryStep.class);
    when(stage_1.getSteps()).thenReturn(Arrays.asList(step_1_1, step_1_2));
    queryPlan.add(stage_1);
    setQueryStatisticsWithQueryPlanOnlyMock(job, queryPlan);
    // No sub step contain the achilio_mv token
    when(step_1_1.getSubsteps()).thenReturn(Arrays.asList("st_1", "FROM"));
    when(step_1_2.getSubsteps()).thenReturn(Arrays.asList("st_1", "st_2 "));
    assertFalse(new BigQueryJob(job).isUseMaterializedView());
    // A sub step contain the achilio_mv token
    when(step_1_1.getSubsteps()).thenReturn(Arrays.asList("st_1", "st_2"));
    when(step_1_2.getSubsteps()).thenReturn(Arrays.asList("st_1", "FROM " + MV_NAME_PREFIX));
    assertTrue(new BigQueryJob(job).isUseMaterializedView());

  }

  @Test
  public void bigQueryJobToQueryUsingCache() {
    Job job = simpleJobMock();
    setQueryStatisticsMock(job, null, null, true, null);
    AQuery query = new BigQueryJob(job);
    assertTrue(query.isUseCache());
  }

  @Test
  public void bigQueryJobToQueryInError() {
    Job job = simpleJobMock();
    BigQueryError error = mock(BigQueryError.class);
    when(error.getMessage()).thenReturn("Really important error message example");
    when(job.getStatus().getError()).thenReturn(error);
    AQuery query = new BigQueryJob(job);
    assertNotNull(query.getError());
    assertEquals("Really important error message example", query.getError());
  }

  @Test
  public void when_bigQueryJobStatisticsNull__thenSetStatisticsToZero() {
    Job job = simpleJobMock();
    when(job.getStatistics()).thenReturn(null);
    AQuery query = new BigQueryJob(job);
    assertNull(query.getStartTime());
    assertFalse(query.isUseCache());
    assertFalse(query.isUseMaterializedView());
    assertEquals(0L, query.getProcessedBytes());
    assertEquals(0L, query.getBilledBytes());
  }

  @Test
  public void when_subStepDoesntContainsTable_thenTableReadIsEmpty() {
    Job job = simpleJobMock();
    assertTrue(new BigQueryJob(job).getJobTableId().isEmpty());
    setQueryPlanSubSteps(job, "");
    assertTrue(new BigQueryJob(job).getJobTableId().isEmpty());
    setQueryPlanSubSteps(job, "FOO", "BAR");
    assertTrue(new BigQueryJob(job).getJobTableId().isEmpty());
    setQueryPlanSubSteps(job, "FOO bar", "BAR foo");
    assertTrue(new BigQueryJob(job).getJobTableId().isEmpty());
  }

  @Test
  public void when_subStepContainsTable_thenExtractTableRead() {
    Job job = simpleJobMock();
    // One sub step
    setQueryPlanSubSteps(job, "FROM a.b.c");
    BigQueryJob actual;
    actual = new BigQueryJob(job);
    assertFalse(actual.getJobTableId().isEmpty());
    assertEquals("a.b.c", actual.getJobTableId().get(0));
    // Multiple sub step
    setQueryPlanSubSteps(job, "FROM a.b.c", "foo", "FROM d.e.f", "bar", "FROM g.h.i");
    actual = new BigQueryJob(job);
    assertFalse(actual.getJobTableId().isEmpty());
    assertEquals(3, actual.getJobTableId().size());
    assertEquals("a.b.c", actual.getJobTableId().get(0));
    assertEquals("d.e.f", actual.getJobTableId().get(1));
    assertEquals("g.h.i", actual.getJobTableId().get(2));
  }

  @Test
  public void when_subStepContainsTableWithoutProject_thenExtractTableReadWithJobProjectId() {
    Job job = simpleJobMock();
    when(job.getJobId()).thenReturn(JobId.of("currentProjectId", "foo"));
    setQueryPlanSubSteps(job, "FROM b.c");
    assertEquals("currentProjectId.b.c", new BigQueryJob(job).getJobTableId().get(0));
  }

  @Test
  public void when_subStepContainsInvalidTable_thenDontExtractTableRead() {
    Job job = simpleJobMock();
    setQueryPlanSubSteps(job, "FROM a.b.c_mvdelta", "FROM a.b.mvdeltatable",
        "FROM b.c.blabla_mvdelta__4_");
    BigQueryJob actual = new BigQueryJob(job);
    assertEquals(1, actual.getJobTableId().size());
    assertEquals("a.b.mvdeltatable", actual.getJobTableId().get(0));
  }

  private void setQueryPlanSubSteps(Job job, String... subSteps) {
    QueryStage stage = mock(QueryStage.class);
    QueryStep step = mock(QueryStep.class);
    when(stage.getSteps()).thenReturn(Arrays.asList(step));
    when(step.getSubsteps()).thenReturn(Arrays.asList(subSteps));
    setQueryStatisticsWithQueryPlanOnlyMock(job, Collections.singletonList(stage));
  }

  private void setStatisticsWithNullFields(Job job) {
    setQueryStatisticsMock(job, null, null, null, null);
  }

  private void setQueryStatisticsMock(Job job, Long totalBytesBilled, Long totalBytesProcessed,
      Boolean cacheHit, Long startTime) {
    setQueryStatisticsMock(job, totalBytesBilled, totalBytesProcessed, cacheHit, startTime,
        Collections.emptyList());
  }

  private void setQueryStatisticsWithQueryPlanOnlyMock(Job job, List<QueryStage> queryPlan) {
    setQueryStatisticsMock(job, null, null, null, null, queryPlan);
  }

  private void setQueryStatisticsMock(Job job, Long totalBytesBilled, Long totalBytesProcessed,
      Boolean cacheHit, Long startTime, List<QueryStage> queryPlan) {
    QueryStatistics queryStatistics = mock(QueryStatistics.class);
    when(queryStatistics.getCacheHit()).thenReturn(cacheHit);
    when(queryStatistics.getTotalBytesBilled()).thenReturn(totalBytesBilled);
    when(queryStatistics.getTotalBytesProcessed()).thenReturn(totalBytesProcessed);
    when(queryStatistics.getStartTime()).thenReturn(startTime);
    when(queryStatistics.getQueryPlan()).thenReturn(queryPlan);
    when(job.getStatistics()).thenReturn(queryStatistics);
  }

  private Job simpleJobMock() {
    Job job = mock(Job.class);
    QueryJobConfiguration configuration = mock(QueryJobConfiguration.class);
    when(configuration.getQuery()).thenReturn("SELECT 1");
    when(job.getConfiguration()).thenReturn(configuration);
    JobStatus status = mock(JobStatus.class);
    when(status.getError()).thenReturn(null);
    when(job.getStatus()).thenReturn(status);
    when(job.getJobId()).thenReturn(JobId.of("job-id"));
    setQueryStatisticsMock(job, 50000L, 10000L, false, 1650394735L);
    return job;
  }

}
