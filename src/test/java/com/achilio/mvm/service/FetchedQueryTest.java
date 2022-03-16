package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchedQueryTest {

  private static final String SIMPLE_QUERY = "SELECT SUM(col) FROM myDataset.myTable";
  private Set<FetchedTable> fetchedTables;

  @Before
  public void setUp() {
    FetchedTable mockTable1 = mock(FetchedTable.class);
    FetchedTable mockTable2 = mock(FetchedTable.class);
    fetchedTables = Sets.newSet(mockTable1, mockTable2);
  }

  @Test
  public void settersGettersAsserts() {
    QueryUsageStatistics statistics = mock(QueryUsageStatistics.class);
    FetchedQuery query = new FetchedQuery(SIMPLE_QUERY);
    assertEquals(query.getQuery(), SIMPLE_QUERY);
    query.setStatistics(statistics);
    query.setReferenceTables(fetchedTables);
    assertEquals(query.getStatistics(), statistics);
    assertEquals(query.getReferenceTables(), fetchedTables);
    query.setUseMaterializedView(false);
    query.setUseCache(false);
    assertFalse(query.isUsingMaterializedView());
    assertFalse(query.isUsingCache());
    query.setUseMaterializedView(true);
    query.setUseCache(true);
    assertTrue(query.isUsingMaterializedView());
    assertTrue(query.isUsingCache());
  }
}
