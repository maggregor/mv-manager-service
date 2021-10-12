package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.extract.fields.AggregateField;
import com.achilio.mvm.service.extract.fields.DefaultFieldSet;
import com.achilio.mvm.service.extract.fields.FieldSet;
import com.achilio.mvm.service.extract.fields.FunctionField;
import com.achilio.mvm.service.extract.fields.ReferenceField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class BigQueryMaterializedViewStatementBuilderTest {

  private final BigQueryMaterializedViewStatementBuilder builder =
      new BigQueryMaterializedViewStatementBuilder();

  private FieldSet fieldSet;

  @Before
  public void setup() {
    fieldSet = new DefaultFieldSet();
    fieldSet.setProjectId("myproject");
    fieldSet.setDataset("mydataset");
    fieldSet.setTable("mytable");
  }

  @Test
  @Ignore
  public void testTableReferenceSerializationWithoutProject() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> builder.buildTableReference(fieldSet));
    assertEquals("Project name is empty or null", exception.getMessage());
  }

  @Test
  public void testTableReferenceSerialization() {
    Assert.assertEquals("`myproject`.`mydataset`.`mytable`", builder.buildTableReference(fieldSet));
  }

  @Test
  public void testSimpleSelectColumnFunctionGroupBy() {
    fieldSet.add(new ReferenceField("col1", "a"));
    fieldSet.add(new FunctionField("TIMESTAMP_TRUNC(ts, DAY)", "b"));
    assertStatementFromFieldSet(
        fieldSet,
        "SELECT col1 AS a, TIMESTAMP_TRUNC(ts, DAY) AS b FROM `myproject`.`mydataset`.`mytable` GROUP BY a, b");
  }

  @Test
  public void testSimpleSelectAggregate() {
    fieldSet.add(new AggregateField("COUNT(*)", "count"));
    assertStatementFromFieldSet(
        fieldSet, "SELECT COUNT(*) AS count FROM `myproject`.`mydataset`.`mytable`");
  }

  @Test
  public void testSelectFunctionOnly() {
    fieldSet.add(new FunctionField("CONCAT(col1, col2)", "a"));
    assertStatementFromFieldSet(
        fieldSet,
        "SELECT CONCAT(col1, col2) AS a FROM `myproject`.`mydataset`.`mytable` GROUP BY a");
  }

  @Test
  public void testSimpleSelectColumnFunctionAggregateGroupBy() {
    fieldSet.add(new ReferenceField("col1", "col1"));
    fieldSet.add(new FunctionField("TIMESTAMP_TRUNC(ts, DAY)", "col2"));
    fieldSet.add(new AggregateField("MAX(ts)", "col3"));
    assertStatementFromFieldSet(
        fieldSet,
        "SELECT col1 AS col1, TIMESTAMP_TRUNC(ts, DAY) AS col2, MAX(ts) AS col3 FROM `myproject`.`mydataset`.`mytable` GROUP BY col1, col2");
  }

  private void assertStatementFromFieldSet(FieldSet fieldSet, String expected) {
    MaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();
    String statement = builder.build(fieldSet);
    Assert.assertEquals(expected, statement);
  }
}
