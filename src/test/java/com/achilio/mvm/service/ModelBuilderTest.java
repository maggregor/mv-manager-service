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
  private final FetchedTable mockFetchedTableSame = mock(FetchedTable.class);
  private final FetchedTable mockFetchedTableWithoutProject = mock(FetchedTable.class);
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
    when(mockFetchedTableWithoutProject.getProjectId()).thenReturn(null);
    when(mockFetchedTableWithoutProject.getDatasetName()).thenReturn("myAgainAnotherDataset");
    when(mockFetchedTableWithoutProject.getTableName()).thenReturn("myAgainAnotherTable");
    when(mockFetchedTableWithoutProject.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTableSame.getProjectId()).thenReturn("myProject");
    when(mockFetchedTableSame.getDatasetName()).thenReturn("myProject");
    when(mockFetchedTableSame.getTableName()).thenReturn("myOtherTable");
    when(mockFetchedTableSame.getColumns()).thenReturn(new HashMap<>());
  }

  @Test
  public void isTableRegistered() {
    assertFalse(builder.isTableRegistered(mockFetchedTable));
    assertFalse(builder.isTableRegistered(mockFetchedTable2));
    builder.registerTable(mockFetchedTable);
    builder.registerTable(mockFetchedTable2);
    assertTrue(builder.isTableRegistered(mockFetchedTable));
    assertTrue(builder.isTableRegistered(mockFetchedTable2));
  }

  @Test
  public void isTableRegisteredWithoutProjectId() {
    assertFalse(builder.isTableRegistered(mockFetchedTableWithoutProject));
    builder.registerTable(mockFetchedTableWithoutProject);
    assertTrue(builder.isTableRegistered(mockFetchedTableWithoutProject));
  }

  @Test
  public void registeredAmbiguousProjectAndDatasetNames() {
    assertFalse(builder.isTableRegistered(mockFetchedTableSame));
    builder.registerTable(mockFetchedTableSame);
    assertTrue(builder.isTableRegistered(mockFetchedTableSame));
  }
}