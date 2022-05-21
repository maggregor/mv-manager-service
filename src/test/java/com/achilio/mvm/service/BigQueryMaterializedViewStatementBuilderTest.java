package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.entities.TableRef;
import com.achilio.mvm.service.entities.TableRef.TableRefType;
import com.achilio.mvm.service.visitors.ATableId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class BigQueryMaterializedViewStatementBuilderTest {

  private QueryPattern queryPattern;

  @Before
  public void setup() {
    queryPattern = new QueryPattern();
    queryPattern.addTableRef(
        new TableRef(ATableId.of("myproject", "mydataset", "mytable"), TableRefType.MAIN));
  }

  @Test
  public void simpleSelectColumnFunctionGroupBy() {
    queryPattern.add(new Field(FieldType.REFERENCE, "col1"));
    queryPattern.add(new Field(FieldType.FUNCTION, "TIMESTAMP_TRUNC(ts, DAY)"));
    assertStatementFromFieldSet(
        queryPattern,
        "SELECT col1 AS a, TIMESTAMP_TRUNC(ts, DAY) AS b FROM `myproject`.`mydataset`.`mytable` GROUP BY a, b");
  }

  @Test
  public void simpleSelectAggregate() {
    queryPattern.add(new Field(FieldType.AGGREGATE, "COUNT(*)"));
    assertStatementFromFieldSet(
        queryPattern, "SELECT COUNT(*) AS count FROM `myproject`.`mydataset`.`mytable`");
  }

  @Test
  public void selectFunctionOnly() {
    queryPattern.add(new Field(FieldType.FUNCTION, "CONCAT(col1, col2)"));
    assertStatementFromFieldSet(
        queryPattern,
        "SELECT CONCAT(col1, col2) AS a FROM `myproject`.`mydataset`.`mytable` GROUP BY a");
  }

  @Test
  public void simpleSelectColumnFunctionAggregateGroupBy() {
    queryPattern.add(new Field(FieldType.REFERENCE, "col1"));
    queryPattern.add(new Field(FieldType.FUNCTION, "TIMESTAMP_TRUNC(ts, DAY)"));
    queryPattern.add(new Field(FieldType.AGGREGATE, "MAX(ts)"));
    assertStatementFromFieldSet(
        queryPattern,
        "SELECT col1 AS col1, TIMESTAMP_TRUNC(ts, DAY) AS col2, MAX(ts) AS col3 FROM `myproject`.`mydataset`.`mytable` GROUP BY col1, col2");
  }

  private void assertStatementFromFieldSet(QueryPattern queryPattern, String expected) {
    MaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();
    String statement = builder.build(queryPattern);
    Assert.assertEquals(expected, statement);
  }
}
