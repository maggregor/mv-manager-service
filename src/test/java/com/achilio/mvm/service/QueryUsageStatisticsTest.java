package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryUsageStatisticsTest {

  private final FetchedQuery query1 = mock(FetchedQuery.class);
  private final FetchedQuery query2 = mock(FetchedQuery.class);

  @Before
  public void setUp() {
    QueryUsageStatistics statistics1 = mock(QueryUsageStatistics.class);
    when(statistics1.getProcessedBytes()).thenReturn(10L);
    when(statistics1.getBilledBytes()).thenReturn(100L);
    when(query1.getStatistics()).thenReturn(statistics1);

    QueryUsageStatistics statistics2 = mock(QueryUsageStatistics.class);
    when(statistics2.getProcessedBytes()).thenReturn(2L);
    when(statistics2.getBilledBytes()).thenReturn(20L);
    when(query2.getStatistics()).thenReturn(statistics2);
  }

  @Test
  public void addQueryUsage() {

  }
}
