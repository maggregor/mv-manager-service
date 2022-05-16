package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.entities.AQuery;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AQueryTest {

  private final String projectId = "myProjectId";
  private final String queryStatement = "SELECT 1";
  private final boolean useMaterializedView = false;
  private final boolean useCache = false;
  private final String googleJobId = "google-id";
  private final String defaultDataset = "myDefaultDataset";

  @Test
  public void simpleValidation() {
    AQuery query =
        new AQuery(
            queryStatement,
            googleJobId,
            projectId,
            defaultDataset,
            useMaterializedView,
            useCache,
            new Date());
    query.setBilledBytes(10L);
    query.setProcessedBytes(100L);
    assertEquals(queryStatement, query.getQuery());
    assertEquals(googleJobId, query.getId());
    assertEquals(projectId, query.getProjectId());
    assertEquals(defaultDataset, query.getDefaultDataset());
    assertFalse(useMaterializedView);
    assertFalse(useCache);
    assertEquals(10L, query.getBilledBytes());
    assertEquals(100L, query.getProcessedBytes());
  }

  @Test
  public void secondConstructorValidation() {
    AQuery query = new AQuery(queryStatement, projectId);
    assertEquals(queryStatement, query.getQuery());
    assertEquals(projectId, query.getProjectId());
    assertFalse(query.isUseCache());
    assertFalse(query.isUseMaterializedView());
    assertNull(query.getDefaultDataset());
    assertNull(query.getStartTime());
  }
}
