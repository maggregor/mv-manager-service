package com.achilio.mvm.service;

import static com.achilio.mvm.service.visitors.ZetaSQLFieldSetExtractVisitor.NOT_REGULAR_COLUMN_PREFIX_TO_SKIP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.visitors.ZetaSQLFieldSetExtractVisitor;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZetaSQLFieldSetExtractVisitorTest {

  @Test
  public void addField() {
    ZetaSQLFieldSetExtractVisitor visitor = new ZetaSQLFieldSetExtractVisitor();
    Field field = mock(ReferenceField.class);
    when(field.name()).thenReturn(NOT_REGULAR_COLUMN_PREFIX_TO_SKIP);
    visitor.addField(field);
    assertTrue(visitor.fieldSet().fields().isEmpty());
    when(field.name()).thenReturn("normalColName");
    visitor.addField(field);
    assertFalse(visitor.fieldSet().fields().isEmpty());
    assertEquals("normalColName", visitor.fieldSet().fields().iterator().next().name());
  }
}
