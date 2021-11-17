package com.achilio.mvm.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.ModelBuilder;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class ModelBuilderTest {

  private final FetchedTable mockFetchedTable = mock(FetchedTable.class);
  private final FetchedTable mockFetchedTable2 = mock(FetchedTable.class);
  private final FetchedTable mockFetchedTable3 = mock(FetchedTable.class);
  private final ModelBuilder builder = new ZetaSQLExtract("myProject");

  @Before
  public void setUp() {
    when(mockFetchedTable.getProjectId()).thenReturn("myProject");
    when(mockFetchedTable.getDatasetName()).thenReturn("myDataset");
    when(mockFetchedTable.getTableName()).thenReturn("myTable");
    when(mockFetchedTable.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTable2.getProjectId()).thenReturn("myProject");
    when(mockFetchedTable2.getDatasetName()).thenReturn("myDataset");
    when(mockFetchedTable2.getTableName()).thenReturn("myOtherTable");
    when(mockFetchedTable2.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTable3.getProjectId()).thenReturn("myProject");
    when(mockFetchedTable3.getDatasetName()).thenReturn("myOtherDataset");
    when(mockFetchedTable3.getTableName()).thenReturn("myOtherTable");
    when(mockFetchedTable3.getColumns()).thenReturn(new HashMap<>());
  }

  @Test
  public void isTableRegistered() {
    assertFalse(builder.isTableRegistered(mockFetchedTable));
    assertFalse(builder.isTableRegistered(mockFetchedTable2));
    assertFalse(builder.isTableRegistered(mockFetchedTable3));
    builder.registerTable(mockFetchedTable);
    builder.registerTable(mockFetchedTable2);
    builder.registerTable(mockFetchedTable3);
    assertTrue(builder.isTableRegistered(mockFetchedTable));
    assertTrue(builder.isTableRegistered(mockFetchedTable2));
    assertTrue(builder.isTableRegistered(mockFetchedTable3));
  }

}
