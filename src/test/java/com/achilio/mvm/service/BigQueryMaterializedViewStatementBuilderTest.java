package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
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
    Set<FetchedTable> tables = new HashSet<>();
    tables.add(new DefaultFetchedTable("myproject", "mydataset", "mytable"));
    fieldSet.setReferenceTables(tables);
  }

  @Test
  public void tableReferenceSerialization() {
    Assert.assertEquals("`myproject`.`mydataset`.`mytable`", builder.buildTableReference(fieldSet));
  }

  @Test
  public void simpleSelectColumnFunctionGroupBy() {
    fieldSet.add(new ReferenceField("col1", "a"));
    fieldSet.add(new FunctionField("TIMESTAMP_TRUNC(ts, DAY)", "b"));
    assertStatementFromFieldSet(
        fieldSet,
        "SELECT col1 AS a, TIMESTAMP_TRUNC(ts, DAY) AS b FROM `myproject`.`mydataset`.`mytable` GROUP BY a, b");
  }

  @Test
  public void simpleSelectAggregate() {
    fieldSet.add(new AggregateField("COUNT(*)", "count"));
    assertStatementFromFieldSet(
        fieldSet, "SELECT COUNT(*) AS count FROM `myproject`.`mydataset`.`mytable`");
  }

  @Test
  public void selectFunctionOnly() {
    fieldSet.add(new FunctionField("CONCAT(col1, col2)", "a"));
    assertStatementFromFieldSet(
        fieldSet,
        "SELECT CONCAT(col1, col2) AS a FROM `myproject`.`mydataset`.`mytable` GROUP BY a");
  }

  @Test
  public void simpleSelectColumnFunctionAggregateGroupBy() {
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
