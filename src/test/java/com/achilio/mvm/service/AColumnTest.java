package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.AColumn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AColumnTest {

  protected abstract AColumn createColumn(String projectId, String tableId, String name);

  @Test
  public void simpleValidationConstructor1() {
    AColumn column;
    column = createColumn("myProjectId", "tableId", "myCol");
    assertEquals("tableId#myCol", column.getColumnId());
    assertEquals("myCol", column.getName());
    assertEquals("myProjectId", column.getProjectId());
  }
}
