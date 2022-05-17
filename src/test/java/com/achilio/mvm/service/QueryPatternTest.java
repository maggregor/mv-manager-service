package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryPatternTest {

  @Test
  public void simpleValidation() {
    QueryPattern queryPattern = new QueryPattern("project", "dataset", "project.dataset.table1");
    assertEquals("project", queryPattern.getProjectId());
    assertEquals("dataset", queryPattern.getDatasetName());
    assertEquals("project.dataset.table1", queryPattern.getTableRefId());
    Field field1 = new Field(FieldType.AGGREGATE, "col1", "project.dataset.table1", "project");
    Field field2 = new Field(FieldType.AGGREGATE, "col2", "project.dataset.table1", "project");
    Field field3 = new Field(FieldType.AGGREGATE, "col2", "project.dataset.table2", "project");
    queryPattern.addField(field1);
    queryPattern.addField(field2);
    queryPattern.addField(field3);
    assertEquals(2, queryPattern.getFields().size());
  }
}
