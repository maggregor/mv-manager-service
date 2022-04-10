package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.visitors.ATableId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaterializedViewTest {

  private static final String PROJECT_ID = "projectId1";
  private static final String DATASET_NAME = "datasetName";
  private static final String TABLE_NAME = "tableName";
  private static final String STATEMENT = "SELECT 1";
  private static final String HASH_CODE = String.valueOf(Math.abs(STATEMENT.hashCode()));
  private static final FindMVJob job = new FindMVJob(PROJECT_ID, 7);
  private static final ATableId tableId = ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME);

  @Test
  public void simpleConstructorValidation() {
    MaterializedView mv = new MaterializedView(job, tableId, STATEMENT);
    assertEquals(job, mv.getInitialJob());
    assertEquals(job, mv.getLastJob());
    assertEquals(PROJECT_ID, mv.getProjectId());
    assertEquals(DATASET_NAME, mv.getDatasetName());
    assertEquals(TABLE_NAME, mv.getTableName());
    assertEquals(MVStatus.NOT_APPLIED, mv.getStatus());
    assertEquals(MVStatusReason.WAITING_APPROVAL, mv.getStatusReason());
    assertEquals(STATEMENT, mv.getStatement());
    assertEquals("tableName_achilio_mv_" + HASH_CODE, mv.getMvName());
    assertEquals(Integer.valueOf(0), mv.getHits());
  }
}
