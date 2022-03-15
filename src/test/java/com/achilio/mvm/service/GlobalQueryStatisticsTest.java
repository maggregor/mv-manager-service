package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.responses.AggregatedStatisticsResponse;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlobalQueryStatisticsTest {

  private final QueryStatistics inStatistics = mock(QueryStatistics.class);
  private final QueryStatistics outStatistics = mock(QueryStatistics.class);
  private final QueryStatistics cachedStatistics = mock(QueryStatistics.class);
  private final GlobalQueryStatistics totalStatistics = new GlobalQueryStatistics();

  @Before
  public void setUp() {
    // IN Stats
    when(inStatistics.getTotalBilledBytes()).thenReturn(8L);
    when(inStatistics.getTotalQueries()).thenReturn(7);
    when(inStatistics.getTotalProcessedBytes()).thenReturn(6L);

    // OUT Stats
    when(outStatistics.getTotalBilledBytes()).thenReturn(80L);
    when(outStatistics.getTotalQueries()).thenReturn(70);
    when(outStatistics.getTotalProcessedBytes()).thenReturn(60L);

    // CACHED Stats
    when(cachedStatistics.getTotalBilledBytes()).thenReturn(800L);
    when(cachedStatistics.getTotalQueries()).thenReturn(700);
    when(cachedStatistics.getTotalProcessedBytes()).thenReturn(600L);
  }

  @Test
  public void noEligibilityStats() {
    GlobalQueryStatistics noEligibilityStats = new GlobalQueryStatistics();
    noEligibilityStats.addStatistic(Scope.IN, inStatistics);
    // Assert Global
    assertEquals(8L, noEligibilityStats.getTotalStatistics().getTotalBilledBytes());
    assertEquals(7, noEligibilityStats.getTotalStatistics().getTotalQueries());
    assertEquals(6L, noEligibilityStats.getTotalStatistics().getTotalProcessedBytes());
    // Assert Scope In
    assertEquals(8L, noEligibilityStats.getDetails().get(Scope.IN).getTotalBilledBytes());
    assertEquals(7, noEligibilityStats.getDetails().get(Scope.IN).getTotalQueries());
    assertEquals(6L, noEligibilityStats.getDetails().get(Scope.IN).getTotalProcessedBytes());
  }

  @Test
  public void addInStats() {
    totalStatistics.addStatistic(Scope.IN, inStatistics);
    // Assert Global
    assertEquals(8L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(7, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(6L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());

    // Assert Scope In
    assertEquals(8L, totalStatistics.getDetails().get(Scope.IN).getTotalBilledBytes());
    assertEquals(7, totalStatistics.getDetails().get(Scope.IN).getTotalQueries());
    assertEquals(6L, totalStatistics.getDetails().get(Scope.IN).getTotalProcessedBytes());
  }

  @Test
  public void addInAndOutStats() {
    totalStatistics.addStatistic(Scope.IN, inStatistics);
    totalStatistics.addStatistic(Scope.OUT, outStatistics);
    assertEquals(88L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(77, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(66L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());

    // Assert Scope In
    assertEquals(8L, totalStatistics.getDetails().get(Scope.IN).getTotalBilledBytes());
    assertEquals(7, totalStatistics.getDetails().get(Scope.IN).getTotalQueries());
    assertEquals(6L, totalStatistics.getDetails().get(Scope.IN).getTotalProcessedBytes());

    // Assert Scope Out
    assertEquals(80L, totalStatistics.getDetails().get(Scope.OUT).getTotalBilledBytes());
    assertEquals(70, totalStatistics.getDetails().get(Scope.OUT).getTotalQueries());
    assertEquals(60L, totalStatistics.getDetails().get(Scope.OUT).getTotalProcessedBytes());
  }

  @Test
  public void addInOutCachedStats() {
    totalStatistics.addStatistic(Scope.IN, inStatistics);
    totalStatistics.addStatistic(Scope.OUT, outStatistics);
    totalStatistics.addStatistic(Scope.CACHED, cachedStatistics);
    assertEquals(888L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(777, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(666L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());

    // Assert Scope In
    assertEquals(8L, totalStatistics.getDetails().get(Scope.IN).getTotalBilledBytes());
    assertEquals(7, totalStatistics.getDetails().get(Scope.IN).getTotalQueries());
    assertEquals(6L, totalStatistics.getDetails().get(Scope.IN).getTotalProcessedBytes());

    // Assert Scope Out
    assertEquals(80L, totalStatistics.getDetails().get(Scope.OUT).getTotalBilledBytes());
    assertEquals(70, totalStatistics.getDetails().get(Scope.OUT).getTotalQueries());
    assertEquals(60L, totalStatistics.getDetails().get(Scope.OUT).getTotalProcessedBytes());

    // Assert Scope Cached
    assertEquals(800L, totalStatistics.getDetails().get(Scope.CACHED).getTotalBilledBytes());
    assertEquals(700, totalStatistics.getDetails().get(Scope.CACHED).getTotalQueries());
    assertEquals(600L, totalStatistics.getDetails().get(Scope.CACHED).getTotalProcessedBytes());
  }

  @Test
  public void aggregatedResponse() {
    GlobalQueryStatistics globalStats = mock(GlobalQueryStatistics.class);
    QueryStatistics totalQueryStats = mock(QueryStatistics.class);
    Map<Scope, QueryStatistics> details = mock(Map.class);
    QueryStatistics queryStatisticsCached = mock(QueryStatistics.class);
    QueryStatistics queryStatisticsIn = mock(QueryStatistics.class);
    when(globalStats.getTotalStatistics()).thenReturn(totalQueryStats);
    when(globalStats.getDetails()).thenReturn(details);
    when(totalQueryStats.getTotalQueries()).thenReturn(1000);
    when(totalQueryStats.getTotalProcessedBytes()).thenReturn(100000L);
    when(details.get(Scope.CACHED)).thenReturn(queryStatisticsCached);
    when(details.get(Scope.IN)).thenReturn(queryStatisticsIn);
    when(queryStatisticsCached.getTotalQueries()).thenReturn(200);
    when(queryStatisticsIn.getTotalQueries()).thenReturn(300);
    AggregatedStatisticsResponse response = new AggregatedStatisticsResponse(globalStats);
    assertEquals(125, response.getAverageScannedBytesPerQuery());
    assertEquals(800L, response.getTotalQueries());
    assertEquals(38.0, response.getPercentQueriesIn(), 0);
    // Check divide by 0
    when(totalQueryStats.getTotalQueries()).thenReturn(0);
    response = new AggregatedStatisticsResponse(globalStats);
    assertEquals(0, response.getAverageScannedBytesPerQuery());
    assertEquals(0, response.getTotalQueries());
    assertEquals(0, response.getPercentQueriesIn(), 0);

    // No stats
    GlobalQueryStatistics stats = new GlobalQueryStatistics();
    response = new AggregatedStatisticsResponse(stats);
    assertEquals(0, response.getAverageScannedBytesPerQuery());
    assertEquals(0, response.getTotalQueries());
    assertEquals(0, response.getPercentQueriesIn(), 0);
  }
}
