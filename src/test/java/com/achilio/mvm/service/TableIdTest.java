package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.achilio.mvm.service.visitors.TableId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableIdTest {

  @Test
  public void datasetTable() {
    TableId tableId = TableId.of("superDataset", "niceTable");
    assertNotNull(tableId);
    assertNull(tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void projectDatasetTable() {
    TableId tableId = TableId.of("goodProject", "superDataset", "niceTable");
    assertNotNull(tableId);
    assertEquals("goodProject", tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseDatasetTable() {
    TableId tableId = TableId.parse("superDataset.niceTable");
    assertNotNull(tableId);
    assertNull(tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseProjectDatasetTable() {
    TableId tableId = TableId.parse("goodProject.superDataset.niceTable");
    assertNotNull(tableId);
    assertEquals("goodProject", tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseTooShort() {
    TableId tableId = TableId.parse("niceTable");
    assertNull(tableId);
  }

  @Test
  public void parseTooLong() {
    TableId tableId = TableId.parse("prefix.prefix2.prefix3.niceTable");
    assertNull(tableId);
  }

}
