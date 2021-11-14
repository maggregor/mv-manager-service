package com.achilio.mvm.service;

import static com.achilio.mvm.service.databases.entities.FetchedTableHelper.createFetchedTable;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.google.zetasql.ZetaSQLType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public abstract class FieldSetExtractTest {

  private static final String[][] SIMPLE_TABLE_COLUMNS =
      new String[][]{
          {"col1", ZetaSQLType.TypeKind.TYPE_STRING.name()},
          {"col2", ZetaSQLType.TypeKind.TYPE_STRING.name()},
          {"col3", ZetaSQLType.TypeKind.TYPE_INT64.name()},
          {"col4", ZetaSQLType.TypeKind.TYPE_INT64.name()},
          {"ts", ZetaSQLType.TypeKind.TYPE_TIMESTAMP.name()}
      };

  private FieldSetAnalyzer extractor;

  protected abstract FieldSetAnalyzer createFieldSetExtract(String projectId,
      Set<FetchedTable> metadata);

  @Before
  public void before() {
    Set<FetchedTable> tables = new HashSet<>();
    tables.add(createFetchedTable("myproject.mydataset.mytable", SIMPLE_TABLE_COLUMNS));
    this.extractor = createFieldSetExtract("myproject", tables);
  }

  @Test
  public void singleReference() {
    final String query = "SELECT col1 FROM mydataset.mytable";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  public void multipleReferences() {
    final String query = "SELECT col1, col2 FROM mydataset.mytable";
    assertExpectedFieldSet(query, new ReferenceField("col1"), new ReferenceField("col2"));
  }

  @Test
  public void whereClauseAndAggregateInSelect() {
    final String query = "SELECT SUM(col3) FROM mydataset.mytable WHERE " + "col3 < 5";
    assertContainsFields(query, new ReferenceField("col3"), new AggregateField("SUM(col3)"));
  }

  @Test
  public void whereClauseSingleReference() {
    final String query = "SELECT 'a' FROM mydataset.mytable WHERE col1 =" + " " + "'a'";
    assertContainsFields(query, new ReferenceField("col1"));
  }

  @Test
  public void whereClauseMultipleReferences() {
    final String query =
        "SELECT 'xxx' FROM mydataset.mytable WHERE col1 " + " = 'xxx' AND col2 = 'yyy'";
    assertContainsFields(query, new ReferenceField("col1"), new ReferenceField("col2"));
  }

  @Test
  public void discoverTablePaths() {
    FetchedQuery fetchedQuery;
    FetchedTable table;
    Iterator<FetchedTable> tableIterator;
    String statement;
    // Simple dataset and table name
    statement = "SELECT 'xxx' FROM mydataset.mytable";
    fetchedQuery = FetchedQueryFactory.createFetchedQuery(statement);
    extractor.discoverFetchedTable(fetchedQuery);
    tableIterator = fetchedQuery.getReferenceTables().iterator();
    Assert.assertTrue(tableIterator.hasNext());
    table = tableIterator.next();
    Assert.assertEquals("mydataset", table.getDatasetName());
    Assert.assertEquals("mytable", table.getTableName());
    // With back quotes
    statement = "SELECT COUNT(*) FROM `mydataset.mytable`";
    fetchedQuery = FetchedQueryFactory.createFetchedQuery(statement);
    extractor.discoverFetchedTable(fetchedQuery);
    tableIterator = fetchedQuery.getReferenceTables().iterator();
    Assert.assertTrue(tableIterator.hasNext());
    table = tableIterator.next();
    Assert.assertEquals("mydataset", table.getDatasetName());
    Assert.assertEquals("mytable", table.getTableName());
  }

  @Test
  public void groupByMultipleReferences() {
    final String query = "SELECT col1, col2, col3 FROM mydataset.mytable GROUP BY col1, col2, col3";
    assertExpectedFieldSet(
        query, new ReferenceField("col1"), new ReferenceField("col2"), new ReferenceField("col3"));
  }

  @Test
  public void simpleSubQueryReference() {
    String q = "SELECT col1 FROM (SELECT col1 FROM mydataset.mytable)";
    assertExpectedFieldSet(q, new ReferenceField("col1"));
  }

  @Test
  public void simpleAliasSubQueryReference() {
    String query = "SELECT myalias FROM ( SELECT col1 as myalias FROM mydataset.mytable )";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  public void extractAggregateWhichContainsOneColumn() {
    String query = "SELECT SUM(col3) FROM mydataset.mytable";
    assertExpectedFieldSet(query, new AggregateField("SUM(col3)"));
  }

  @Test
  public void extractAggregateWhichContainsComplexExpression() {
    String query;
    query = "SELECT SUM(col3 + col4) FROM mydataset.mytable";
    assertContainsFields(query, new AggregateField("SUM(col3 + col4)"));
    query = "SELECT SUM(col4 + col3) FROM mydataset.mytable";
    assertContainsFields(query, new AggregateField("SUM(col4 + col3)"));
  }

  @Test
  public void extractAggregatesAndReferences() {
    String query;
    query = "SELECT col1, MIN(col3) FROM mydataset.mytable GROUP BY col1";
    assertContainsFields(query, new ReferenceField("col1"), new AggregateField("MIN(col3)"));
    query = "SELECT col1, MIN(col3) FROM mydataset.mytable WHERE col1 = " + "'xxx' GROUP BY col1";
    assertContainsFields(query, new ReferenceField("col1"), new AggregateField("MIN(col3)"));
    query = "SELECT col1, MIN(col3) FROM mydataset.mytable WHERE col2 = " + "'xxx' GROUP BY col1";
    assertContainsFields(
        query,
        new ReferenceField("col1"),
        new ReferenceField("col2"),
        new AggregateField("MIN(col3)"));
  }

  @Test
  @Ignore
  // In column with small column count, we (maybe) want extract and optimize.
  public void notExtractAStarSelect() {
    String query = "SELECT * FROM mydataset.mytable";
    assertZeroFields(query);
    query = "SELECT col1, col2, col3, col4 FROM mydataset.mytable";
    assertZeroFields(query);
    query =
        "SELECT col3, col4 FROM mydataset.mytable WHERE col1 = 'xxx'"
            + " AND col2 = 'xxx' GROUP BY col3, col4";
    assertZeroFields(query);
    query = "SELECT col1, col2, col3, col4 FROM mydataset.mytable GROUP BY col1, col2, col3, col4";
    assertZeroFields(query);
    query = "SELECT * FROM (SELECT * FROM mydataset.mytable)";
    assertZeroFields(query);
    query = "SELECT * FROM (SELECT * FROM (SELECT col1, col2, col3, col4 FROM mydataset.mytable))";
    assertZeroFields(query);
  }

  @Test
  public void extractExpression() {
    String query = "SELECT col1 = 'x' FROM mydataset.mytable";
    assertExpectedFieldSet(query, new FunctionField("col1 = (\"x\")"));
    query = "SELECT col3 + col4 FROM mydataset.mytable";
    assertExpectedFieldSet(query, new FunctionField("col3 + col4"));
    query = "SELECT IF(col3 < 4, 'bonjour', 'aurevoir') FROM mydataset" + ".mytable";
    assertExpectedFieldSet(
        query, new FunctionField("IF(col3 < 4, " + "\"bonjour\", \"aurevoir\")"));
    query = "SELECT CASE WHEN col1 = 'x' THEN 'a' ELSE 'b' END FROM " + "mydataset.mytable";
    assertExpectedFieldSet(
        query, new FunctionField("CASE WHEN (col1 = (\"x\")) THEN (\"a\") ELSE (\"b\") END"));
  }

  @Test // TODO: Support my-project.mydataset.mytable
  public void extractTablesWithProject() {
    final String query = "SELECT col1 FROM mydataset.mytable GROUP BY " + "col1";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  public void aliasWithoutGroupBy() {
    String query = "SELECT col1 as myalias FROM mydataset.mytable";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  public void aliasOnFunctionsShouldNotBeExtracted() {
    String query =
        "SELECT col1 as myalias, COUNT(*) as count FROM mydataset.mytable GROUP BY myalias";
    assertExpectedFieldSet(query, new ReferenceField("col1"), new AggregateField("COUNT(*)"));
  }

  @Test
  public void aliasOnFunctionsInGroupByShouldNotBeExtracted() {
    String query =
        "SELECT col1, CONCAT(col1, col2) as myalias, col3 FROM mydataset.mytable GROUP BY col1, myalias, col3";
    assertExpectedFieldSet(
        query,
        new ReferenceField("col1"),
        new ReferenceField("col3"),
        new FunctionField("CONCAT(col1, col2)"));
  }

  @Test
  public void noUnderscoreTimestamp() {
    String query =
        "SELECT col1 AS col1, TIMESTAMP_TRUNC(ts, DAY) AS __timestamp, sum(col3) AS "
            + "SUM_tip_amount__f2041 FROM mydataset.mytable GROUP BY col1, __timestamp ORDER BY "
            + "SUM_tip_amount__f2041 DESC LIMIT 10000;";
    assertExpectedFieldSet(
        query,
        new ReferenceField("col1"),
        new FunctionField("TIMESTAMP_TRUNC(ts, DAY)"),
        new AggregateField("SUM(col3)"));
  }

  public void assertZeroFields(String query) {
    final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
    Assert.assertEquals(FieldSetFactory.EMPTY_FIELD_SET, actual);
    Assert.assertTrue("Actual FieldSet should be empty", actual.fields().isEmpty());
  }

  private void assertContainsFields(String query, Field... fields) {
    final FieldSet expected = FieldSetHelper.createFieldSet(fields);
    final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
    for (Field field : expected.fields()) {
      final String msg =
          String.format("One field is missing: %s.\nActual fields: %s", field.name(), actual);
      Assert.assertTrue(msg, actual.fields().contains(field));
    }
  }

  private void assertExpectedFieldSet(String query, Field... fields) {
    final FieldSet expected = FieldSetHelper.createFieldSet(fields);
    final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
    assertExpectedFieldSet(expected, actual);
  }

  private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
    Assert.assertNotNull("Actual FieldSet is null.", actual);
    Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
  }
}
