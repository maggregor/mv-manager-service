package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.controllers.responses.ATableResponse;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.ATable.TableType;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ATableResponseTest {

  private static final String PROJECT_ID = "myBestProjectId";
  private static final String DATASET_NAME = "mySuperDataset";
  private static final String TABLE_NAME = "theBestTableName";

  protected abstract ATableResponse createTableResponse(String project, String dataset,
      String table, float cost, int queryCount, ATable.TableType type, Date createdAt,
      Date lastModifiedAt, Long numRows);

  @Test
  public void simpleAsserts() {
    final Date createdAt = new Date(1652786773714L);
    final Date lastModifiedAt = new Date(1652786786505L);
    ATableResponse response = createTableResponse(PROJECT_ID, DATASET_NAME, TABLE_NAME, 1234F, 123,
        TableType.TABLE, createdAt, lastModifiedAt, 1000L);
    assertEquals(PROJECT_ID, response.getProjectId());
    assertEquals(DATASET_NAME, response.getDatasetName());
    assertEquals(TABLE_NAME, response.getTableName());
    assertEquals(new Float(1234), response.getCost());
    assertEquals(123, response.getQueryCount());
    assertEquals(TableType.TABLE, response.getType());
    assertEquals(createdAt, response.getCreatedAt());
    assertEquals(lastModifiedAt, response.getLastModifiedAt());
    assertEquals(new Long(1000), response.getNumRows());
  }
}
