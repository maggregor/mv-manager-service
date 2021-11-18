package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
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
      FieldSetHelper.createFieldSet(new AggregateField("SUM(col1)"),
          new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1 =
      FieldSetHelper.createFieldSet(new AggregateField("SUM(col1)"),
          new FunctionField("CONCAT(col1, col2)"));
  private static final FieldSet FIELD_SET_SAME_AS_1_DIFF_SORT =
      FieldSetHelper.createFieldSet(new FunctionField("CONCAT(col1, col2)"),
          new AggregateField("SUM(col1)"));
  private static final FieldSet FIELD_SET_2 =
      FieldSetHelper.createFieldSet(new ReferenceField("col1"), new ReferenceField("col2"));
  private static final FieldSet FIELD_SET_3 =
      FieldSetHelper.createFieldSet(
          new ReferenceField("col1"),
          new AggregateField("MAX(col2)"),
          new ReferenceField("col3"));

  @Test
  public void equals() {
    assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1);
    assertEquals(FIELD_SET_1, FIELD_SET_1.clone());
    assertEquals(FIELD_SET_1, FIELD_SET_SAME_AS_1_DIFF_SORT);
    assertNotEquals(FIELD_SET_1, FIELD_SET_2);
    assertNotEquals(FIELD_SET_1, FIELD_SET_3);
    assertNotEquals(FIELD_SET_1, null);
    assertEquals(FIELD_SET_1, FIELD_SET_1);
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
  public void statistics() {
    QueryUsageStatistics mockStatistics = mock(QueryUsageStatistics.class);
    when(mockStatistics.getBilledBytes()).thenReturn(10L);
    when(mockStatistics.getProcessedBytes()).thenReturn(20L);
    FieldSet fieldSet = FieldSetHelper.createFieldSet(new ReferenceField("a"));
    fieldSet.setStatistics(mockStatistics);
    assertEquals(mockStatistics, fieldSet.getStatistics());
    assertEquals(10L, fieldSet.getStatistics().getBilledBytes());
    assertEquals(20L, fieldSet.getStatistics().getProcessedBytes());
  }
}
