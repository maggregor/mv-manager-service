package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.achilio.mvm.service.entities.AchilioQuery;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AchilioQueryTest {
  private final String projectId = "myProjectId";
  private final FetcherQueryJob fetcherQueryJob = new FetcherQueryJob(projectId);
  private final String queryStatement = "SELECT 1";
  private final boolean useMaterializedView = false;
  private final boolean useCache = false;
  private final LocalDate startTime = LocalDate.of(2020, 1, 8);
  private final String table1 = "myTable1";
  private final String table2 = "myTable2";
  private final Set<String> refTables = new HashSet<>(Arrays.asList(table1, table1, table2));
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);


  @Test
  public void simpleValidation() {
    AchilioQuery achilioQuery = new AchilioQuery(fetcherQueryJob, queryStatement, useMaterializedView, useCache, startTime, refTables, stats);
    assertEquals(queryStatement, achilioQuery.getQuery());
    assertFalse(useMaterializedView);
    assertFalse(useCache);
    assertEquals(startTime, achilioQuery.getStartTime());
    assertEquals(refTables, achilioQuery.getRefTables());
    assertEquals(1, achilioQuery.getQueryCount());
    assertEquals(10L, achilioQuery.getBilledBytes());
    assertEquals(100L, achilioQuery.getProcessedBytes());
  }
}
