package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlobalQueryStatisticsTest {

  GlobalQueryStatistics totalStatistics = new GlobalQueryStatistics(true);
  private final QueryStatistics inStatistics = mock(QueryStatistics.class);
  private final QueryStatistics outStatistics = mock(QueryStatistics.class);
  private final QueryStatistics cachedStatistics = mock(QueryStatistics.class);

  @Before
  public void setUp() {
    // IN Stats
    when(inStatistics.getEligible()).thenReturn(10);
    when(inStatistics.getIneligible()).thenReturn(9);
    when(inStatistics.getTotalBilledBytes()).thenReturn(8L);
    when(inStatistics.getTotalQueries()).thenReturn(7);
    when(inStatistics.getTotalProcessedBytes()).thenReturn(6L);
    Map<QueryIneligibilityReason, MutableInt> ineligibility =
        Maps.newHashMap(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE, new MutableInt(1));
    ineligibility.put(QueryIneligibilityReason.PARSING_FAILED, new MutableInt(2));
    ineligibility.put(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE, new MutableInt(3));
    ineligibility.put(
        QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN, new MutableInt(4));
    when(inStatistics.getIneligibleReasons()).thenReturn(ineligibility);

    // OUT Stats
    when(outStatistics.getEligible()).thenReturn(100);
    when(outStatistics.getIneligible()).thenReturn(90);
    when(outStatistics.getTotalBilledBytes()).thenReturn(80L);
    when(outStatistics.getTotalQueries()).thenReturn(70);
    when(outStatistics.getTotalProcessedBytes()).thenReturn(60L);
    when(outStatistics.getIneligibleReasons()).thenReturn(ineligibility);

    // CACHED Stats
    when(cachedStatistics.getEligible()).thenReturn(1000);
    when(cachedStatistics.getIneligible()).thenReturn(900);
    when(cachedStatistics.getTotalBilledBytes()).thenReturn(800L);
    when(cachedStatistics.getTotalQueries()).thenReturn(700);
    when(cachedStatistics.getTotalProcessedBytes()).thenReturn(600L);
    when(cachedStatistics.getIneligibleReasons()).thenReturn(ineligibility);
  }

  @Test
  public void noEligibilityStats() {
    GlobalQueryStatistics noEligibilityStats = new GlobalQueryStatistics();
    noEligibilityStats.addStatistic(GlobalQueryStatistics.SCOPE_IN, inStatistics);
    // Assert Global
    assertEquals(-1, noEligibilityStats.getTotalStatistics().getEligible());
    assertEquals(-1, noEligibilityStats.getTotalStatistics().getIneligible());
    assertEquals(8L, noEligibilityStats.getTotalStatistics().getTotalBilledBytes());
    assertEquals(7, noEligibilityStats.getTotalStatistics().getTotalQueries());
    assertEquals(6L, noEligibilityStats.getTotalStatistics().getTotalProcessedBytes());
    assertNull(
        noEligibilityStats
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertNull(
        noEligibilityStats
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertNull(
        noEligibilityStats
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertNull(
        noEligibilityStats
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope In
    assertEquals(
        10, noEligibilityStats.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getEligible());
    assertEquals(
        9, noEligibilityStats.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getIneligible());
    assertEquals(
        8L,
        noEligibilityStats.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalBilledBytes());
    assertEquals(
        7, noEligibilityStats.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalQueries());
    assertEquals(
        6L,
        noEligibilityStats
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        noEligibilityStats
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        noEligibilityStats
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        noEligibilityStats
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        noEligibilityStats
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));
  }

  @Test
  public void addInStats() {
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_IN, inStatistics);
    // Assert Global
    assertEquals(10, totalStatistics.getTotalStatistics().getEligible());
    assertEquals(9, totalStatistics.getTotalStatistics().getIneligible());
    assertEquals(8L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(7, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(6L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope In
    assertEquals(
        10, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getEligible());
    assertEquals(
        9, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getIneligible());
    assertEquals(
        8L, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalBilledBytes());
    assertEquals(
        7, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalQueries());
    assertEquals(
        6L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));
  }

  @Test
  public void addInAndOutStats() {
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_IN, inStatistics);
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_OUT, outStatistics);
    assertEquals(110, totalStatistics.getTotalStatistics().getEligible());
    assertEquals(99, totalStatistics.getTotalStatistics().getIneligible());
    assertEquals(88L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(77, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(66L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(6),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(8),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope In
    assertEquals(
        10, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getEligible());
    assertEquals(
        9, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getIneligible());
    assertEquals(
        8L, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalBilledBytes());
    assertEquals(
        7, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalQueries());
    assertEquals(
        6L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope Out
    assertEquals(
        100, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getEligible());
    assertEquals(
        90, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getIneligible());
    assertEquals(
        80L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalBilledBytes());
    assertEquals(
        70, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalQueries());
    assertEquals(
        60L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));
  }

  @Test
  public void addInOutCachedStats() {
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_IN, inStatistics);
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_OUT, outStatistics);
    totalStatistics.addStatistic(GlobalQueryStatistics.SCOPE_CACHED, cachedStatistics);
    assertEquals(1110, totalStatistics.getTotalStatistics().getEligible());
    assertEquals(999, totalStatistics.getTotalStatistics().getIneligible());
    assertEquals(888L, totalStatistics.getTotalStatistics().getTotalBilledBytes());
    assertEquals(777, totalStatistics.getTotalStatistics().getTotalQueries());
    assertEquals(666L, totalStatistics.getTotalStatistics().getTotalProcessedBytes());
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(6),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(9),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(12),
        totalStatistics
            .getTotalStatistics()
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope In
    assertEquals(
        10, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getEligible());
    assertEquals(
        9, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getIneligible());
    assertEquals(
        8L, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalBilledBytes());
    assertEquals(
        7, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalQueries());
    assertEquals(
        6L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_IN).getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_IN)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope Out
    assertEquals(
        100, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getEligible());
    assertEquals(
        90, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getIneligible());
    assertEquals(
        80L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalBilledBytes());
    assertEquals(
        70, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalQueries());
    assertEquals(
        60L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_OUT).getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_OUT)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));

    // Assert Scope Cached
    assertEquals(
        1000, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_CACHED).getEligible());
    assertEquals(
        900, totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_CACHED).getIneligible());
    assertEquals(
        800L,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_CACHED).getTotalBilledBytes());
    assertEquals(
        700,
        totalStatistics.getDetails().get(GlobalQueryStatistics.SCOPE_CACHED).getTotalQueries());
    assertEquals(
        600L,
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_CACHED)
            .getTotalProcessedBytes());
    assertEquals(
        new MutableInt(1),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_CACHED)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.DOES_NOT_FILTER_OR_AGGREGATE));
    assertEquals(
        new MutableInt(2),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_CACHED)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.PARSING_FAILED));
    assertEquals(
        new MutableInt(3),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_CACHED)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN_TYPE));
    assertEquals(
        new MutableInt(4),
        totalStatistics
            .getDetails()
            .get(GlobalQueryStatistics.SCOPE_CACHED)
            .getIneligibleReasons()
            .get(QueryIneligibilityReason.MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN));
  }
}