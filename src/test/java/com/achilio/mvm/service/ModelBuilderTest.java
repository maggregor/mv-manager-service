package com.achilio.mvm.service;

import static com.achilio.mvm.service.MockHelper.createMockATable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.ModelBuilder;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import com.achilio.mvm.service.visitors.ZetaSQLModelBuilder;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

public class ModelBuilderTest {

  private final ATableId tableId1 = ATableId.of("myProject", "myDataset", "myTable");
  private final ATableId tableId2 = ATableId.of("myProject", "myDataset", "myOtherTable");
  private final ATableId tableIdWithoutProject = ATableId.of("myDataset", "myTable");
  private final ATableId tableIdSame = ATableId.of("same", "same", "myTable");

  private final ATable mockATable = createMockATable(tableId1);
  private final ATable mockATable2 = createMockATable(tableId2);
  private final ATable mockATableSame = createMockATable(tableIdSame);
  private final ModelBuilder builder =
      new ZetaSQLExtract("myProject", new HashSet<>(Arrays.asList(mockATableSame)));
  private final ATable mockATableWithoutProject = createMockATable(tableIdWithoutProject);

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
    assertEquals("myProject", ((ZetaSQLModelBuilder) builder).getProjectId());
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
