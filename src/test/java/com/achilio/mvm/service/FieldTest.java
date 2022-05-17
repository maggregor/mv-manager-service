package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FieldTest {

  @Test
  public void simpleValidation() {
    Field field1 = new Field(FieldType.AGGREGATE, "col1", "project.dataset.table1", "project");
    Field field2 = new Field(FieldType.AGGREGATE, "col2", "project.dataset.table1", "project");
    Field field3 = new Field(FieldType.AGGREGATE, "col1", "project.dataset.table1", "project");
    assertEquals(FieldType.AGGREGATE, field1.getFieldType());
    assertEquals("col1", field1.getColName());
    assertEquals("project.dataset.table1", field1.getTableRefId());
    assertEquals(field1.hashCode(), field3.hashCode());
    assertEquals(field1, field3);
    assertNotEquals(field1.hashCode(), field2.hashCode());
    assertNotEquals(field1, field2);
  }
}
