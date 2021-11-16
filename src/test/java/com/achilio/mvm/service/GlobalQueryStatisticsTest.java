package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlobalQueryStatisticsTest {

  @Test
  public void addStatistics() {
    QueryStatistics statistics1 = mock(QueryStatistics.class);
    when(statistics1.getTotalQueries()).thenReturn(2);
  }
}
