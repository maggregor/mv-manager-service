package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryStatisticsTest {

  private final Query query1 = mock(Query.class);
  private final Query query2 = mock(Query.class);

  @Before
  public void setUp() {
    when(query1.getProcessedBytes()).thenReturn(10L);
    when(query1.getBilledBytes()).thenReturn(100L);

    when(query2.getProcessedBytes()).thenReturn(100L);
    when(query2.getBilledBytes()).thenReturn(1000L);
  }

  @Test
  public void incrementQuery() {
    QueryStatistics totalStatistics;
    totalStatistics = new QueryStatistics(Lists.newArrayList(query1));
    assertEquals(1, totalStatistics.getTotalQueries());
    totalStatistics = new QueryStatistics(Lists.newArrayList(query1, query2));
    assertEquals(2, totalStatistics.getTotalQueries());
  }

}
