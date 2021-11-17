package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public class OptimizationResultTest {

  private final static String SIMPLE_STATEMENT = "CREATE MATERIALIZED VIEW ...";
  private final Optimization mockOptimization = mock(Optimization.class);
  private final FetchedTable mockFetchedTable = mock(FetchedTable.class);

  @Before
  public void setUp() {
    when(mockFetchedTable.getProjectId()).thenReturn("myProject");
    when(mockFetchedTable.getDatasetName()).thenReturn("myDataset");
    when(mockFetchedTable.getTableName()).thenReturn("myTable");
  }

  @Test
  public void simpleValidation() {
    OptimizationResult result = new OptimizationResult(
        mockOptimization, mockFetchedTable, SIMPLE_STATEMENT);
    assertEquals(SIMPLE_STATEMENT, result.getStatement());
    assertEquals("myProject", result.getProjectId());
    assertEquals("myDataset", result.getDatasetName());
    assertEquals("myTable", result.getTableName());
    assertEquals(mockOptimization, result.getOptimization());
  }

  @Test
  public void simpleValidationNull() {
    OptimizationResult result = new OptimizationResult();
    assertNull(result.getStatement());
    assertNull(result.getProjectId());
    assertNull(result.getDatasetName());
    assertNull(result.getTableName());
    assertNull(result.getId());
    assertNull(result.getOptimization());
  }

}
