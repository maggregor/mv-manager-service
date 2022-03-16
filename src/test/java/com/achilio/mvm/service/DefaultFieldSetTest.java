package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.visitors.FieldSetIneligibilityReason;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DefaultFieldSetTest {

  private static final FieldSet FIELD_SET_1 =
      FieldSetHelper.createFieldSet(
          new AggregateField("SUM(col1)"), new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1 =
      FieldSetHelper.createFieldSet(
          new AggregateField("SUM(col1)"), new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1_DIFF_SORT =
      FieldSetHelper.createFieldSet(
          new FunctionField("CONCAT(col1, col2)"), new AggregateField("SUM(col1)"));
  private static final FieldSet FIELD_SET_2 =
      FieldSetHelper.createFieldSet(new ReferenceField("col1"), new ReferenceField("col2"));
  private static final FieldSet FIELD_SET_3 =
      FieldSetHelper.createFieldSet(
          new ReferenceField("col1"), new AggregateField("MAX(col2)"), new ReferenceField("col3"));
  private static final FieldSet FIELD_SET_4 =
      FieldSetHelper.createFieldSet(new AggregateField("SUM(col10000)"));

  @Test
  public void equals() {
    assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1);
    assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1_DIFF_SORT);
    assertNotEquals(FIELD_SET_1, FIELD_SET_2);
    assertNotEquals(FIELD_SET_1, FIELD_SET_3);
    assertNotEquals(FIELD_SET_1, null);
    assertEquals(FIELD_SET_1, FIELD_SET_1);
    assertNotEquals(FIELD_SET_1, FIELD_SET_4);
  }

  @Test
  public void fields() {
    assertEquals(2, FIELD_SET_1.fields().size());
    assertEquals(1, FIELD_SET_1.aggregates().size());
    assertEquals(1, FIELD_SET_1.functions().size());
    assertEquals(2, FIELD_SET_2.fields().size());
    assertEquals(2, FIELD_SET_2.references().size());
    assertEquals(0, FIELD_SET_2.aggregates().size());
    assertEquals(3, FIELD_SET_3.fields().size());
    assertEquals(1, FIELD_SET_3.aggregates().size());
    assertEquals(2, FIELD_SET_3.references().size());
  }

  @Test
  public void add() {
    final FieldSet expected =
        FieldSetHelper.createFieldSet(new ReferenceField("a"), new FunctionField("b"));
    FieldSet actual = new DefaultFieldSet();
    assertNotEquals(expected, actual);
    actual.add(new ReferenceField("a"));
    actual.add(new FunctionField("b"));
    assertEquals(expected, actual);
  }

  @Test
  public void addRemoveEligibleReason() {
    FieldSet fieldSet = FieldSetHelper.createFieldSet();
    assertEquals(0, fieldSet.getIneligibilityReasons().size());
    fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY);
    assertEquals(1, fieldSet.getIneligibilityReasons().size());
    fieldSet.removeIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY);
    assertEquals(0, fieldSet.getIneligibilityReasons().size());
  }

  @Test
  public void isEligible() {
    FieldSet fieldSet = FieldSetHelper.createFieldSet();
    assertTrue(fieldSet.isEligible());
    fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY);
    assertFalse(fieldSet.isEligible());
  }

  @Test
  public void clearEligibleReasons() {
    FieldSet fieldSet = FieldSetHelper.createFieldSet();
    fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN);
    fieldSet.addIneligibilityReason(FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY);
    assertFalse(fieldSet.isEligible());
    assertEquals(2, fieldSet.getIneligibilityReasons().size());
    fieldSet.clearIneligibilityReasons();
    assertTrue(fieldSet.isEligible());
    assertEquals(0, fieldSet.getIneligibilityReasons().size());
  }
}
