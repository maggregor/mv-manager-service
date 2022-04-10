package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.MaterializedView;

public class AssertHelper {

  public static void assertMVEquals(MaterializedView expected, MaterializedView actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getProjectId(), actual.getProjectId());
    assertEquals(expected.getDatasetName(), actual.getDatasetName());
    assertEquals(expected.getTableName(), actual.getTableName());
    assertEquals(expected.getMvName(), actual.getMvName());
    assertEquals(expected.getMvUniqueName(), actual.getMvUniqueName());
    assertEquals(expected.getStatement(), actual.getStatement());
    assertEquals(expected.getStatementHashCode(), actual.getStatementHashCode());
  }
}
