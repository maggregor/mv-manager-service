package com.achilio.mvm.service;

import static com.achilio.mvm.service.MockHelper.tableMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.ModelBuilder;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import org.junit.Test;

public class ModelBuilderTest {

  private final ATableId tableId1 = ATableId.of("myProject", "myDataset", "myTable");
  private final ATableId tableId2 = ATableId.of("myProject", "myDataset", "myOtherTable");
  private final ATableId tableIdWithoutProject = ATableId.of("myDataset", "myTable");
  private final ATableId tableIdSame = ATableId.of("same", "same", "myTable");

  private final ATable mockATable = tableMock(tableId1);
  private final ATable mockATable2 = tableMock(tableId2);
  private final ATable mockATableSame = tableMock(tableIdSame);
  private final ATable mockATableWithoutProject = tableMock(tableIdWithoutProject);
  private final ModelBuilder builder = new ZetaSQLExtract();

  @Test
  public void isTableRegistered() {
    assertFalse(builder.isTableRegistered(mockATable));
    assertFalse(builder.isTableRegistered(mockATable2));
    builder.registerTable(mockATable);
    builder.registerTable(mockATable2);
    assertTrue(builder.isTableRegistered(mockATable));
    assertTrue(builder.isTableRegistered(mockATable2));
  }

  @Test
  public void getters() {
    assertTrue(builder.getTables().isEmpty());
  }

  @Test
  public void isTableRegisteredWithoutProjectId() {
    assertFalse(builder.isTableRegistered(mockATableWithoutProject));
    builder.registerTable(mockATableWithoutProject);
    assertTrue(builder.isTableRegistered(mockATableWithoutProject));
  }

  @Test
  public void registeredAmbiguousProjectAndDatasetNames() {
    assertFalse(builder.isTableRegistered(mockATableSame));
    builder.registerTable(mockATableSame);
    assertTrue(builder.isTableRegistered(mockATableSame));
  }
}
