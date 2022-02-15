package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FieldTest {

  @Test
  public void equals() {
    Field ref1 = new ReferenceField("col1", "a");
    Field ref1clone = new ReferenceField("col1", "a");
    Field ref2 = new ReferenceField("col2", "a");
    assertEquals(ref1, ref1);
    assertEquals(ref1, ref1clone);
    assertNotEquals(ref1, ref2);
  }
}
