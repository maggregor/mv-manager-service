package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.fields.AggregateField;
import com.achilio.mvm.service.extract.fields.DefaultFieldSet;
import com.achilio.mvm.service.extract.fields.FieldSet;
import com.achilio.mvm.service.extract.fields.FunctionField;
import com.achilio.mvm.service.extract.fields.ReferenceField;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DefaultFieldSetTest {

  private static final FieldSet FIELD_SET_1 =
      FieldSetHelper.createFieldSet(
          10, 100, new AggregateField("SUM(col1)"), new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1 =
      FieldSetHelper.createFieldSet(
          10, 100, new AggregateField("SUM(col1)"), new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1_DIFF_SORT =
      FieldSetHelper.createFieldSet(
          10, 100, new FunctionField("CONCAT(col1, col2)"), new AggregateField("SUM(col1)"));
  private static final FieldSet FIELD_SET_2 =
      FieldSetHelper.createFieldSet(
          20, 1000, new ReferenceField("col1"), new ReferenceField("col2"));
  private static final FieldSet FIELD_SET_3 =
      FieldSetHelper.createFieldSet(
          40,
          4000,
          new ReferenceField("col1"),
          new AggregateField("MAX(col2)"),
          new ReferenceField("col3"));

  @Test
  public void testEquals() {
    Assert.assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1);
    Assert.assertEquals(FIELD_SET_1, FIELD_SET_1.clone());
    Assert.assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1_DIFF_SORT);
    Assert.assertNotEquals(FIELD_SET_1, FIELD_SET_2);
    Assert.assertNotEquals(FIELD_SET_1, FIELD_SET_3);
  }

  @Test
  public void testFields() {
    Assert.assertEquals(2, FIELD_SET_1.fields().size());
    Assert.assertEquals(1, FIELD_SET_1.aggregates().size());
    Assert.assertEquals(1, FIELD_SET_1.functions().size());
    Assert.assertEquals(2, FIELD_SET_2.fields().size());
    Assert.assertEquals(2, FIELD_SET_2.references().size());
    Assert.assertEquals(0, FIELD_SET_2.aggregates().size());
    Assert.assertEquals(3, FIELD_SET_3.fields().size());
    Assert.assertEquals(1, FIELD_SET_3.aggregates().size());
    Assert.assertEquals(2, FIELD_SET_3.references().size());
  }

  @Test
  public void testAdd() {
    final FieldSet expected =
        FieldSetHelper.createFieldSet(new ReferenceField("a"), new FunctionField("b"));
    FieldSet actual = new DefaultFieldSet();
    Assert.assertNotEquals(expected, actual);
    actual.add(new ReferenceField("a"));
    actual.add(new FunctionField("b"));
    Assert.assertEquals(expected, actual);
  }
}
