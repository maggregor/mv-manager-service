package com.achilio.mvm.service;

import static com.achilio.mvm.service.BigQueryMockHelper.simpleTableMock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.BigQueryTable;
import com.google.cloud.bigquery.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryTableTest extends ATableTest {

  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final long giga = 1024L * 1024L * 1024L;

  @Override
  protected ATable createTable(String projectId, String datasetName, String tableName) {
    return new BigQueryTable(simpleTableMock(projectId, datasetName, tableName));
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
    Table bqTable = simpleTableMock(projectId, datasetName, tableName);
    when(bqTable.getNumBytes()).thenReturn(numBytes);
    when(bqTable.getNumLongTermBytes()).thenReturn(numBytesLongTerm);
    ATable table = new BigQueryTable(bqTable);
    assertEquals(new Float(expectedCost), table.getCost());
  }
}
