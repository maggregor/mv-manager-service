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
    QueryPattern queryPattern = new QueryPattern();
    queryPattern.setProjectId("project");
    assertEquals("project", queryPattern.getProjectId());
    Field field1 = new Field(FieldType.AGGREGATE, "col1");
    Field field2 = new Field(FieldType.AGGREGATE, "col2");
    Field field3 = new Field(FieldType.AGGREGATE, "col2");
    queryPattern.add(field1);
    queryPattern.add(field2);
    queryPattern.add(field3);
    assertEquals(2, queryPattern.getFields().size());
  }
}
