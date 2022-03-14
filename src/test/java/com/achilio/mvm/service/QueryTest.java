package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryTest {

  private final String projectId = "myProjectId";
  private final FetcherQueryJob fetcherQueryJob = new FetcherQueryJob(projectId);
  private final String queryStatement = "SELECT 1";
  private final boolean useMaterializedView = false;
  private final boolean useCache = false;
  private final String googleJobId = "google-id";
  private final LocalDate startTime = LocalDate.of(2020, 1, 8);
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);

  @Test
  public void simpleValidation() {
    Query query =
        new Query(
            fetcherQueryJob,
            queryStatement,
            googleJobId,
            projectId,
            useMaterializedView,
            useCache,
            startTime,
            stats);
    assertEquals(queryStatement, query.getQuery());
    assertEquals(googleJobId, query.getId());
    assertEquals(projectId, query.getProjectId());
    assertFalse(useMaterializedView);
    assertFalse(useCache);
    assertEquals(startTime, query.getStartTime());
    assertEquals(10L, query.getBilledBytes());
    assertEquals(100L, query.getProcessedBytes());
  }
}
