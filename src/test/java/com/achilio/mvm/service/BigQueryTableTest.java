package com.achilio.mvm.service;

import static com.achilio.mvm.service.BigQueryMockHelper.simpleBigQueryExternalMock;
import static com.achilio.mvm.service.BigQueryMockHelper.simpleBigQueryTableMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.bigquery.BigQueryTable;
import com.google.cloud.bigquery.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryTableTest extends ATableTest {

  private static final String PROJECT_ID = "myProjectId";
  private static final String DATASET_NAME = "myDataset";
  private static final String TABLE_NAME = "myTable";
  private final long giga = 1024L * 1024L * 1024L;

  @Override
  protected ATable createTable(String projectId, String datasetName, String tableName) {
    return new BigQueryTable(simpleBigQueryTableMock(PROJECT_ID, DATASET_NAME, TABLE_NAME));
  }

  @Test
  public void when_SchemaIsNull_thenDontThrows() {
    Table table = simpleBigQueryExternalMock(PROJECT_ID, DATASET_NAME, TABLE_NAME);
    when(table.getDefinition().getSchema()).thenReturn(null);
    assertNotNull(new BigQueryTable(table).getColumns());
    assertTrue(new BigQueryTable(table).getColumns().isEmpty());
  }

  @Test
  public void cost_whenNoLongTermBytes() {
    assertBigQueryTableCost(.2F, giga * 10L, 0L);
    assertBigQueryTableCost(1.0F, giga * 50L, 0L);
    assertBigQueryTableCost(2.0F, giga * 100L, 0L);
    assertBigQueryTableCost(10.0F, giga * 500L, 0L);
  }

  @Test
  public void cost_whenPartLongTermBytes() {
    assertBigQueryTableCost(.15F, giga * 10L, giga * 5L);
    assertBigQueryTableCost(.8F, giga * 50L, giga * 20L);
    assertBigQueryTableCost(1.2F, giga * 100L, giga * 80L);
    assertBigQueryTableCost(5.05F, giga * 500L, giga * 495L);
  }

  @Test
  public void cost_whenFullLongTermBytes() {
    assertBigQueryTableCost(.1F, giga * 10L, giga * 10L);
    assertBigQueryTableCost(1.F, giga * 100L, giga * 100L);
    assertBigQueryTableCost(5.F, giga * 500L, giga * 500L);
    assertBigQueryTableCost(10F, giga * 1000L, giga * 1000L);
  }

  private void assertBigQueryTableCost(float expectedCost, long numBytes, long numBytesLongTerm) {
    Table bqTable = simpleBigQueryTableMock(PROJECT_ID, DATASET_NAME, TABLE_NAME);
    when(bqTable.getNumBytes()).thenReturn(numBytes);
    when(bqTable.getNumLongTermBytes()).thenReturn(numBytesLongTerm);
    ATable table = new BigQueryTable(bqTable);
    assertEquals(new Float(expectedCost), table.getCost());
  }
}
