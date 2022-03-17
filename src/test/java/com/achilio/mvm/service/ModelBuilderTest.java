package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.ModelBuilder;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import com.achilio.mvm.service.visitors.ZetaSQLModelBuilder;
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
    ATableId tableId1 = ATableId.of("myProject", "myDataset", "myTable");
    ATableId tableId2 = ATableId.of("myProject", "myDataset", "myOtherTable");
    ATableId tableIdWithoutProject = ATableId.of("myDataset", "myTable");
    ATableId tableIdSame = ATableId.of("same", "same", "myTable");
    when(mockFetchedTable.getTableId()).thenReturn(tableId1);
    when(mockFetchedTable.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTable2.getTableId()).thenReturn(tableId2);
    when(mockFetchedTable2.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTableWithoutProject.getTableId()).thenReturn(tableIdWithoutProject);
    when(mockFetchedTableWithoutProject.getColumns()).thenReturn(new HashMap<>());
    when(mockFetchedTableSame.getTableId()).thenReturn(tableIdSame);
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
  public void getters() {
    assertTrue(builder.getTables().isEmpty());
    assertEquals("myProject", ((ZetaSQLModelBuilder) builder).getProjectId());
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
