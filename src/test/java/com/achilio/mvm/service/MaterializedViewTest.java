package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
  private static final String TABLE_NAME1 = "tableName1";
  private static final String TABLE_NAME2 = "tableName2";
  private static final String STATEMENT1 = "SELECT 1";
  private static final String STATEMENT2 = "SELECT 2";
  private static final String HASH_CODE = String.valueOf(Math.abs(STATEMENT1.hashCode()));
  private static final FindMVJob JOB1 = new FindMVJob(PROJECT_ID, 7);
  private static final FindMVJob JOB2 = new FindMVJob(PROJECT_ID, 14);
  private static final ATableId TABLE_ID1 = ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME1);
  private static final ATableId TABLE_ID2 = ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME2);

  @Test
  public void simpleConstructorValidation() {
    MaterializedView mv = new MaterializedView(JOB1, TABLE_ID1, STATEMENT1);
    assertEquals(JOB1, mv.getInitialJob());
    assertEquals(JOB1, mv.getLastJob());
    assertEquals(PROJECT_ID, mv.getProjectId());
    assertEquals(DATASET_NAME, mv.getDatasetName());
    assertEquals(TABLE_NAME1, mv.getTableName());
    assertEquals(MVStatus.NOT_APPLIED, mv.getStatus());
    assertEquals(MVStatusReason.WAITING_APPROVAL, mv.getStatusReason());
    assertEquals(STATEMENT1, mv.getStatement());
    assertEquals("tableName1_achilio_mv_" + HASH_CODE, mv.getMvName());
    assertEquals("achilio_mv_" + HASH_CODE, mv.getMvDisplayName());
    assertEquals(Integer.valueOf(0), mv.getHits());
    assertTrue(mv.isNotApplied());
    assertFalse(mv.isApplied());
    mv.setStatus(MVStatus.APPLIED);
    assertTrue(mv.isApplied());
    mv.setStatus(MVStatus.OUTDATED);
    assertTrue(mv.isApplied());
  }

  @Test
  public void equalsAndHashCode() {
    MaterializedView mv1 = new MaterializedView(JOB1, TABLE_ID1, STATEMENT1);
    MaterializedView mv2 = new MaterializedView(JOB1, TABLE_ID1, STATEMENT1);
    MaterializedView mv3 = new MaterializedView(JOB1, TABLE_ID1, STATEMENT2);
    MaterializedView mv4 = new MaterializedView(JOB1, TABLE_ID2, STATEMENT2);
    MaterializedView mv5 = new MaterializedView(JOB2, TABLE_ID1, STATEMENT1);
    assertEquals(mv1, mv1);
    assertEquals(mv1, mv2);
    assertEquals(mv1, mv5);
    assertNotEquals("notAView", mv1);
    assertNotEquals(mv1, mv3);
    assertNotEquals(mv3, mv4);

    assertEquals(mv1.hashCode(), mv1.hashCode());
    assertEquals(mv1.hashCode(), mv2.hashCode());
    assertEquals(mv1.hashCode(), mv5.hashCode());
    assertNotEquals("notAView", mv1.hashCode());
    assertNotEquals(mv1.hashCode(), mv3.hashCode());
    assertNotEquals(mv3.hashCode(), mv4.hashCode());
  }
}
