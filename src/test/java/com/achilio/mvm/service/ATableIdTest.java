package com.achilio.mvm.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.achilio.mvm.service.visitors.ATableId;
import com.google.cloud.bigquery.TableId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ATableIdTest {

  @Test
  public void datasetTable() {
    ATableId tableId = ATableId.of("superDataset", "niceTable");
    assertNotNull(tableId);
    assertNull(tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void projectDatasetTable() {
    ATableId tableId = ATableId.of("goodProject", "superDataset", "niceTable");
    assertNotNull(tableId);
    assertEquals("goodProject", tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseDatasetTable() {
    ATableId tableId = ATableId.parse("superDataset.niceTable");
    assertNotNull(tableId);
    assertNull(tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseProjectDatasetTable() {
    ATableId tableId = ATableId.parse("goodProject.superDataset.niceTable");
    assertNotNull(tableId);
    assertEquals("goodProject", tableId.getProject());
    assertEquals("superDataset", tableId.getDataset());
    assertEquals("niceTable", tableId.getTable());
  }

  @Test
  public void parseTooShort() {
    ATableId tableId = ATableId.parse("niceTable");
    assertNull(tableId);
  }

  @Test
  public void parseTooLong() {
    ATableId tableId = ATableId.parse("prefix.prefix2.prefix3.niceTable");
    assertNull(tableId);
  }

  @Test
  public void parseWithBackticks() {
    ATableId expected = ATableId.of("goodProject", "superDataset", "niceTable");
    ATableId tableId;
    tableId = ATableId.parse("`goodProject.superDataset.niceTable`");
    assertNotNull(tableId);
    assertEquals(expected, tableId);
    tableId = ATableId.parse("`goodProject`.superDataset.niceTable");
    assertNotNull(tableId);
    assertEquals(expected, tableId);
    tableId = ATableId.parse("`goodProject`.`superDataset`.`niceTable`");
    assertNotNull(tableId);
    assertEquals(expected, tableId);
  }

  @Test
  public void convertGoogleTableId() {
    final String expectedProjectId = "MyProJect";
    final String expectedDatasetName = "DaTas123";
    final String expectedTableName = "myTaaaable";
    TableId googleTableId;
    ATableId actual;
    // ADataset, Table
    googleTableId = TableId.of(expectedDatasetName, expectedTableName);
    actual = ATableId.fromGoogleTableId(googleTableId);
    assertEquals(ATableId.of(expectedDatasetName, expectedTableName), actual);
    // Project, ADataset, Table
    googleTableId = TableId.of(expectedProjectId, expectedDatasetName, expectedTableName);
    actual = ATableId.fromGoogleTableId(googleTableId);
    assertEquals(ATableId.of(expectedProjectId, expectedDatasetName, expectedTableName), actual);
  }

  @Test
  public void asPath() {
    ATableId tableId1 = ATableId.of("dataset", "table");
    ATableId tableId2 = ATableId.of("project", "dataset", "table");
    assertEquals("dataset.table", tableId1.asPath());
    assertEquals("project.dataset.table", tableId2.asPath());
  }

  @Test
  public void equals() {
    ATableId tableId = ATableId.of("dataset", "table");
    assertEquals(tableId, tableId);
    assertNotEquals(tableId, null);
  }
}
