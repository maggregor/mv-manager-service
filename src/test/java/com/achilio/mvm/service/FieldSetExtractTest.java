package com.achilio.mvm.service;

import static com.achilio.mvm.service.FetchedTableHelper.createFetchedTable;
import static com.achilio.mvm.service.FieldSetHelper.createFieldSet;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.FieldSetExtract;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.TableId;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class FieldSetExtractTest {

  private static final String PROJECT_ID = "myproject";
  private static final String[][] SIMPLE_TABLE_COLUMNS =
      new String[][] {
        {"col1", ZetaSQLType.TypeKind.TYPE_STRING.name()},
        {"col2", ZetaSQLType.TypeKind.TYPE_STRING.name()},
        {"col3", ZetaSQLType.TypeKind.TYPE_INT64.name()},
        {"col4", ZetaSQLType.TypeKind.TYPE_INT64.name()},
        {"ts", ZetaSQLType.TypeKind.TYPE_TIMESTAMP.name()}
      };
  private static final TableId MAIN_TABLE_ID = TableId.of(PROJECT_ID, "mydataset", "mytable");
  private static final TableId SECONDARY_TABLE_ID =
      TableId.of(PROJECT_ID, "mydataset", "myothertable");
  private final FetchedTable MAIN_TABLE = createFetchedTable(MAIN_TABLE_ID, SIMPLE_TABLE_COLUMNS);
  private final FetchedTable SECONDARY_TABLE =
      createFetchedTable(SECONDARY_TABLE_ID, SIMPLE_TABLE_COLUMNS);
  private FieldSetExtract extractor;

  protected abstract FieldSetExtract createFieldSetExtract(
      String projectId, Set<FetchedTable> metadata);

  @Before
  public void setUp() {
    this.extractor =
        createFieldSetExtract("myproject", Sets.newLinkedHashSet(MAIN_TABLE, SECONDARY_TABLE));
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
    final String query = "SELECT SUM(col3) FROM mydataset.mytable WHERE col3 < 5";
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

  @Test // Extract a list must be the same that extract an element
  public void extractListTest() {
    FetchedQuery query1 = new FetchedQuery(PROJECT_ID, "SELECT col1 FROM mydataset.mytable");
    FetchedQuery query2 =
        new FetchedQuery(PROJECT_ID, "SELECT col2 FROM mydataset.mytable GROUP BY col2");
    List<FetchedQuery> queries = new ArrayList<>(Arrays.asList(query1, query2));
    List<FieldSet> fieldSets = extractor.extract(queries);
    assertExpectedFieldSet(extractor.extract(query1), fieldSets.get(0));
    assertExpectedFieldSet(extractor.extract(query2), fieldSets.get(1));
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
  public void zeroFieldsAfterExtract() {
    String query = "SELECT 1";
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

  @Test
  public void extractTablesWithoutProject() {
    final String query = "SELECT col1 FROM mydataset.mytable GROUP BY " + "col1";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  public void extractTablesWithProject() {
    final String query = "SELECT col1 FROM myproject.mydataset.mytable GROUP BY " + "col1";
    assertExpectedFieldSet(query, new ReferenceField("col1"));
  }

  @Test
  @Ignore
  public void extractTablesWithFullQuotedPath() {
    final String query = "SELECT col1 FROM `myproject.mydataset.mytable` GROUP BY " + "col1";
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

  @Test
  public void groupBy123() {
    String query =
        "SELECT col1, col2, col3 FROM mydataset.mytable WHERE col3 IN (100, 200) AND ts > TIMESTAMP_SUB(ts, INTERVAL 24 * 31 * 6 hour)  GROUP BY 1, 2, 3";
    assertContainsFields(
        query,
        new ReferenceField("col1"),
        new ReferenceField("col2"),
        new ReferenceField("col3"),
        new ReferenceField("ts"));
  }

  @Test
  public void with() {
    final TableId REF_TABLE = TableId.of("mydataset", "mytable");
    final FieldSet EXPECTED = createFieldSet(REF_TABLE, new ReferenceField("col3"));
    String q = "WITH a AS (SELECT col3 FROM mydataset.mytable GROUP BY 1) SELECT SUM(col3) FROM a";
    List<FieldSet> fieldSetList = extractor.extractAll(PROJECT_ID, q);
    assertEquals(1, fieldSetList.size());
    assertEquals(EXPECTED, fieldSetList.get(0));
  }

  @Test
  public void subQueryInExpr() {
    final FieldSet EXPECTED_1 = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    final FieldSet EXPECTED_2 = createFieldSet(SECONDARY_TABLE_ID, new AggregateField("SUM(col3)"));
    String q =
        "SELECT col1, (SELECT SUM(col3) as a FROM mydataset.mytable) as a FROM mydataset.mytable GROUP BY 1";
    List<FieldSet> fieldSetList = extractor.extractAll(PROJECT_ID, q);
    assertEquals(2, fieldSetList.size());
    assertEquals(EXPECTED_1, fieldSetList.get(0));
    assertEquals(EXPECTED_2, fieldSetList.get(1));
  }

  @Test
  public void leftJoin() {
    final FieldSet EXPECTED_1 = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    String q =
        "SELECT a.col1 FROM mydataset.mytable a LEFT JOIN mydataset.myothertable b USING(col1) GROUP BY 1";
    List<FieldSet> fieldSetList = extractor.extractAll(PROJECT_ID, q);
    assertEquals(1, fieldSetList.size());
    FieldSet fs = fieldSetList.get(0);
    assertEquals(1, fs.getJoinTables().size());
    assertTrue(fs.getJoinTables().containsKey(SECONDARY_TABLE_ID));
    assertEquals(JoinType.LEFT, fs.getJoinTables().get(SECONDARY_TABLE_ID));
    assertExpectedFieldSet(EXPECTED_1, fs);
  }

  public void assertZeroFields(String query) {
    final List<FieldSet> actual = FieldSetHelper.statementToFieldSet(PROJECT_ID, query, extractor);
    assertTrue("Actual FieldSet should be empty", actual.isEmpty());
  }

  private void assertContainsFields(String query, Field... fields) {
    final FieldSet expected = createFieldSet(fields);
    final List<FieldSet> fieldSets =
        FieldSetHelper.statementToFieldSet(PROJECT_ID, query, extractor);
    assertEquals(1, fieldSets.size(), "One FieldSet expected not " + fieldSets.size());
    FieldSet actual = fieldSets.get(0);
    for (Field field : expected.fields()) {
      final String msg =
          String.format("One field is missing: %s.\nActual fields: %s", field.name(), actual);
      assertTrue(msg, actual.fields().contains(field));
    }
  }

  private void assertExpectedFieldSet(String query, Field... fields) {
    final FieldSet expected = createFieldSet(fields);
    final List<FieldSet> fieldSets =
        FieldSetHelper.statementToFieldSet(PROJECT_ID, query, extractor);
    assertEquals(1, fieldSets.size(), "Only one FieldSet expected not " + fieldSets.size());
    FieldSet actual = fieldSets.get(0);
    assertExpectedFieldSet(expected, actual);
  }

  private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
    Assert.assertNotNull("Actual FieldSet is null.", actual);
    Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
  }
}
