package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryStatisticsTest {

  private final FetchedQuery query1 = mock(FetchedQuery.class);
  private final FetchedQuery query2 = mock(FetchedQuery.class);
  private final QueryUsageStatistics statistics1 = mock(QueryUsageStatistics.class);
  private final QueryUsageStatistics statistics2 = mock(QueryUsageStatistics.class);
  ;

  @Before
  public void setUp() {
    when(statistics1.getProcessedBytes()).thenReturn(10L);
    when(statistics1.getBilledBytes()).thenReturn(100L);
    when(query1.getStatistics()).thenReturn(statistics1);

    when(statistics2.getProcessedBytes()).thenReturn(2L);
    when(statistics2.getBilledBytes()).thenReturn(20L);
    when(query2.getStatistics()).thenReturn(statistics2);
  }

  @Test
  public void addQueryUsage() {
    QueryStatistics totalStatistics1 = new QueryStatistics(Lists.newArrayList(query1));
    assertEquals(10L, totalStatistics1.getTotalProcessedBytes());
    assertEquals(100L, totalStatistics1.getTotalBilledBytes());

    QueryStatistics totalStatistics2 = new QueryStatistics(Lists.newArrayList(query1, query2));
    assertEquals(12L, totalStatistics2.getTotalProcessedBytes());
    assertEquals(120L, totalStatistics2.getTotalBilledBytes());
  }

  @Test
  public void incrementQuery() {
    QueryStatistics totalStatistics;
    totalStatistics = new QueryStatistics(Lists.newArrayList(query1));
    assertEquals(1, totalStatistics.getTotalQueries());
    totalStatistics = new QueryStatistics(Lists.newArrayList(query1, query2));
    assertEquals(2, totalStatistics.getTotalQueries());
  }

  @Test
  public void addQueryUsageStatistics() {
    QueryUsageStatistics statistics = new QueryUsageStatistics();
    statistics.setProcessedBytes(10L);
    statistics.setBilledBytes(20L);
    assertEquals(10L, statistics.getProcessedBytes());
    assertEquals(20L, statistics.getBilledBytes());
    //
    when(statistics1.getProcessedBytes()).thenReturn(100L);
    when(statistics1.getBilledBytes()).thenReturn(300L);
    statistics.addQueryUsageStatistics(statistics1);
    assertEquals(110L, statistics.getProcessedBytes());
    assertEquals(320L, statistics.getBilledBytes());
    //
    when(statistics2.getProcessedBytes()).thenReturn(300L);
    when(statistics2.getBilledBytes()).thenReturn(1000L);
    statistics.addQueryUsageStatistics(statistics2);
    assertEquals(410L, statistics.getProcessedBytes());
    assertEquals(1320L, statistics.getBilledBytes());
  }
}
