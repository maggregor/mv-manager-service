package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.BigQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.QueryJobConfiguration;
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
    Query query = new BigQueryJob(job);
    assertFalse(query.isUseCache());
    assertEquals(0L, query.getBilledBytes());
    assertEquals(0L, query.getProcessedBytes());
    assertNull(query.getStartTime());
  }

  @Test
  public void bigQueryJobToQuery() {
    Job job = simpleJobMock();
    Query query = new BigQueryJob(job);
    assertEquals("SELECT 1", query.getQuery());
    assertTrue(StringUtils.isEmpty(query.getError()));
    assertEquals("job-id", query.getId());
    assertEquals(1650394735L, query.getStartTime().toInstant().toEpochMilli());
    assertEquals(10000L, query.getProcessedBytes());
    assertEquals(50000L, query.getBilledBytes());
  }

  @Test
  public void bigQueryJobToQueryInError() {
    Job job = simpleJobMock();
    BigQueryError error = mock(BigQueryError.class);
    when(error.getMessage()).thenReturn("Really important error message example");
    when(job.getStatus().getError()).thenReturn(error);
    Query query = new BigQueryJob(job);
    assertNotNull(query.getError());
    assertEquals("Really important error message example", query.getError());
  }

  private void setStatisticsWithNullFields(Job job) {
    setQueryStatisticsMock(job, null, null, null, null);
  }

  private void setQueryStatisticsMock(Job job, Long totalBytesBilled, Long totalBytesProcessed,
      Boolean cacheHit, Long startTime) {
    QueryStatistics queryStatistics = mock(QueryStatistics.class);
    when(queryStatistics.getCacheHit()).thenReturn(cacheHit);
    when(queryStatistics.getTotalBytesBilled()).thenReturn(totalBytesBilled);
    when(queryStatistics.getTotalBytesProcessed()).thenReturn(totalBytesProcessed);
    when(queryStatistics.getStartTime()).thenReturn(startTime);
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
