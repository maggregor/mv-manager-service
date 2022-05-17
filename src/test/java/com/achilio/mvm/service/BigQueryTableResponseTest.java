package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.responses.ATableResponse;
import com.achilio.mvm.service.controllers.responses.BigQueryTableResponse;
import com.achilio.mvm.service.entities.ATable.TableType;
import com.achilio.mvm.service.entities.BigQueryTable;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryTableResponseTest extends ATableResponseTest {

  @Test
  public void assertImplementationFields() {
    BigQueryTable table = mock(BigQueryTable.class);
    when(table.getNumBytes()).thenReturn(1234L);
    when(table.getNumLongTermBytes()).thenReturn(4723L);
    BigQueryTableResponse response = new BigQueryTableResponse(table);
    assertEquals(1234L, response.getNumBytes());
    assertEquals(4723L, response.getNumLongTermBytes());
  }

  @Override
  protected ATableResponse createTableResponse(String project, String dataset, String tableName,
      float cost, int queryCount, TableType type, Date createdAt, Date lastModifiedAt,
      Long numRows) {
    BigQueryTable table = mock(BigQueryTable.class);
    when(table.getProjectId()).thenReturn(project);
    when(table.getDatasetName()).thenReturn(dataset);
    when(table.getTableName()).thenReturn(tableName);
    when(table.getCost()).thenReturn(cost);
    when(table.getQueryCount()).thenReturn(queryCount);
    when(table.getType()).thenReturn(type);
    when(table.getNumRows()).thenReturn(numRows);
    when(table.getCreatedAt()).thenReturn(createdAt);
    when(table.getLastModifiedAt()).thenReturn(lastModifiedAt);
    return new BigQueryTableResponse(table);
  }
}
