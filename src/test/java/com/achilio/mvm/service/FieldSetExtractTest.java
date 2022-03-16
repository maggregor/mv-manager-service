package com.achilio.mvm.service;

import static com.achilio.mvm.service.FetchedTableHelper.createFetchedTable;
import static com.achilio.mvm.service.FieldSetHelper.createFieldSet;
import static com.achilio.mvm.service.visitors.FieldSetIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN;
import static com.achilio.mvm.service.visitors.FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.FieldSetExtract;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.TableId;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
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
  private static final TableId THIRD_TABLE_ID = TableId.of(PROJECT_ID, "mydataset", "mythirdtable");
  private final FetchedTable MAIN_TABLE = createFetchedTable(MAIN_TABLE_ID, SIMPLE_TABLE_COLUMNS);
  private final FetchedTable SECONDARY_TABLE =
      createFetchedTable(SECONDARY_TABLE_ID, SIMPLE_TABLE_COLUMNS);
  private final FetchedTable THIRD_TABLE = createFetchedTable(THIRD_TABLE_ID, SIMPLE_TABLE_COLUMNS);
  private FieldSetExtract extractor;

  protected abstract FieldSetExtract createFieldSetExtract(
      String projectId, Set<FetchedTable> metadata);

  @Before
  public void setUp() {
    this.extractor =
        createFieldSetExtract(
            PROJECT_ID, Sets.newLinkedHashSet(MAIN_TABLE, SECONDARY_TABLE, THIRD_TABLE));
  }

  @Test
  public void singleReference() {
    final FieldSet EXPECTED =
        fieldSetBuilder()
            .setRefTable(MAIN_TABLE_ID)
            .addRef("col1")
            .addIneligibility(DOES_NOT_CONTAIN_A_GROUP_BY)
            .build();
    final String query = "SELECT col1 FROM mydataset.mytable";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void multipleReferences() {
    final FieldSet EXPECTED =
        fieldSetBuilder()
            .addRef("col1")
            .addRef("col2")
            .addIneligibility(DOES_NOT_CONTAIN_A_GROUP_BY)
            .build();
    final String query = "SELECT col1, col2 FROM mydataset.mytable";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void whereClauseAndAggregateInSelect() {
    final FieldSet EXPECTED = fieldSetBuilder().addAgg("SUM(col3)").addRef("col3").build();
    final String query = "SELECT SUM(col3) FROM mydataset.mytable WHERE col3 < 5";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void whereClauseSingleReference() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").build();
    final String query = "SELECT 'a' FROM mydataset.mytable WHERE col1 = 'a'";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void whereClauseMultipleReferences() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").addRef("col2").build();
    final String query = "SELECT 'x' FROM mydataset.mytable WHERE col1 = 'x' AND col2 = 'y'";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void simpleGroupBy() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").build();
    final String query = "SELECT col1 FROM mydataset.mytable GROUP BY 1";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void groupByMultipleReferences() {
    final FieldSet EXPECTED = fieldSetBuilder().addRefs("col1", "col2", "col3").build();
    final String query = "SELECT col1, col2, col3 FROM mydataset.mytable GROUP BY 1, 2, 3";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void simpleSubQueryReference() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addRef("col1").addIneligibility(DOES_NOT_CONTAIN_A_GROUP_BY).build();
    final String query = "SELECT col1 FROM (SELECT col1 FROM mydataset.mytable)";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void simpleAliasSubQueryReference() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addRef("col1").addIneligibility(DOES_NOT_CONTAIN_A_GROUP_BY).build();
    final String query = "SELECT myalias FROM ( SELECT col1 as myalias FROM mydataset.mytable )";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void extractAggregateWhichContainsOneColumn() {
    final FieldSet EXPECTED = fieldSetBuilder().addAgg("SUM(col3)").build();
    String query = "SELECT SUM(col3) FROM mydataset.mytable";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void extractAggregateThatContainExpression() {
    final FieldSet EXPECTED_1 = fieldSetBuilder().addAgg("SUM(col3 + col4)").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder().addAgg("SUM((col4 + col3) + 10)").build();
    assertExpectedFieldSet("SELECT SUM(col3 + col4) FROM mydataset.mytable", EXPECTED_1);
    assertExpectedFieldSet("SELECT SUM(col4 + col3 + 10) FROM mydataset.mytable", EXPECTED_2);
  }

  @Test
  public void extractOneReferenceOneAggregate() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").addAgg("AVG(col3)").build();
    final String query = "SELECT col1, AVG(col3) FROM mydataset.mytable GROUP BY col1";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void extractOneReferenceOneAggregateWhereClause() {
    final FieldSet EXPECTED = fieldSetBuilder().addRefs("col1", "col2").addAgg("AVG(col3)").build();
    final String query = "SELECT col1, AVG(col3) FROM mydataset.mytable WHERE col2 = '' GROUP BY 1";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void zeroFieldsAfterExtract() {
    assertZeroFields("SELECT 1");
    assertZeroFields("SELECT 'a'");
  }

  @Test
  public void extractExpressionBoolean() {
    final FieldSet EXPECTED = fieldSetBuilder().addFunc("col1 = (\"x\")").build();
    assertExpectedFieldSet("SELECT col1 = 'x' FROM mydataset.mytable GROUP BY 1", EXPECTED);
  }

  @Test
  public void extractExpressionPlus() {
    final FieldSet EXPECTED = fieldSetBuilder().addFunc("col3 + col4").build();
    assertExpectedFieldSet("SELECT col3 + col4 FROM mydataset.mytable GROUP BY 1", EXPECTED);
  }

  @Test
  public void extractExpressionIf() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addFunc("IF(col3 < 4, \"bonjour\", \"aurevoir\")").build();
    assertExpectedFieldSet(
        "SELECT IF(col3 < 4, 'bonjour', 'aurevoir') FROM mydataset.mytable GROUP BY 1", EXPECTED);
  }

  @Test
  public void extractExpressionCaseWhen() {
    final FieldSet EXPECTED =
        fieldSetBuilder()
            .addFunc("CASE WHEN (col1 = (\"x\")) THEN (\"a\") ELSE (\"b\") END")
            .build();
    assertExpectedFieldSet(
        "SELECT CASE WHEN col1 = 'x' THEN 'a' ELSE 'b' END FROM mydataset.mytable GROUP BY 1",
        EXPECTED);
  }

  @Test
  public void queryWithProjectPrefix() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").build();
    final String query = "SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void queryWithEntireQuotedPath() {
    final String query = "SELECT col1 FROM `myproject.mydataset.mytable` GROUP BY col1";
    assertExpectedFieldSet(query, fieldSetBuilder().addRef("col1").build());
  }

  @Test
  public void queryWithQuotedPath() {
    final String query = "SELECT col1 FROM `myproject`.`mydataset`.`mytable` GROUP BY col1";
    assertExpectedFieldSet(query, fieldSetBuilder().addRef("col1").build());
  }

  @Test
  public void queryWithPartialPath() {
    final String query = "SELECT col1 FROM `myproject`.mydataset.mytable GROUP BY col1";
    assertExpectedFieldSet(query, fieldSetBuilder().addRef("col1").build());
  }

  @Test
  public void aliasWithoutGroupBy() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addRef("col1").addIneligibility(DOES_NOT_CONTAIN_A_GROUP_BY).build();
    String query = "SELECT col1 as myalias FROM mydataset.mytable";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void aliasOnFunctionsShouldNotBeExtracted() {
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col1").addFunc("COUNT(*)").build();
    String query = "SELECT col1 as alias, COUNT(*) as count FROM mydataset.mytable GROUP BY alias";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void aliasOnFunctionsInGroupByShouldNotBeExtracted() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addRef("col1").addFunc("CONCAT(col1, col2)").addRef("col3").build();
    String query =
        "SELECT col1, CONCAT(col1, col2) as myalias, col3 FROM mydataset.mytable GROUP BY col1, myalias, col3";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void noUnderscoreTimestamp() {
    final FieldSet EXPECTED =
        fieldSetBuilder()
            .addRef("col1")
            .addFunc("TIMESTAMP_TRUNC(ts, DAY)")
            .addAgg("SUM(col3)")
            .build();
    String query =
        "SELECT col1 AS col1, TIMESTAMP_TRUNC(ts, DAY) AS __timestamp, sum(col3) AS "
            + "SUM_tip_amount__f2041 FROM mydataset.mytable GROUP BY col1, __timestamp ORDER BY "
            + "SUM_tip_amount__f2041 DESC LIMIT 10000;";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void timestampSubFuncWhereIn() {
    final FieldSet EXPECTED = fieldSetBuilder().addRefs("col1", "col2", "col3", "ts").build();
    String query =
        "SELECT col1, col2, col3 FROM mydataset.mytable WHERE col3 IN (100, 200) AND ts > TIMESTAMP_SUB(ts, INTERVAL 24 * 31 * 6 hour)  GROUP BY 1, 2, 3";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void selectWithoutAggregateAndGroupBy() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    EXPECTED.addIneligibilityReason(DOES_NOT_CONTAIN_A_GROUP_BY);
    String query = "SELECT col1 FROM mydataset.mytable";
    assertExpectedFieldSet(query, EXPECTED);
  }

  @Test
  public void with() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col3"));
    String q = "WITH a AS (SELECT col3 FROM mydataset.mytable GROUP BY 1) SELECT SUM(col3) FROM a";
    List<FieldSet> fieldSetList = extractor.extractAll(PROJECT_ID, q);
    assertEquals(1, fieldSetList.size());
    assertEquals(EXPECTED, fieldSetList.get(0));
  }

  @Test
  public void subQueryInExpr() {
    final FieldSet EXPECTED_1 = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    final FieldSet EXPECTED_2 = createFieldSet(SECONDARY_TABLE_ID, new AggregateField("SUM(col3)"));
    String s =
        "SELECT col1, (SELECT SUM(col3) FROM mydataset.myothertable) as a FROM mydataset.mytable GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED_1, EXPECTED_2);
  }

  @Test
  public void leftJoin() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    EXPECTED.addJoinTable(SECONDARY_TABLE_ID, JoinType.LEFT);
    EXPECTED.addIneligibilityReason(CONTAINS_UNSUPPORTED_JOIN);
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "LEFT JOIN mydataset.myothertable b USING(col1) "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  public void rightJoin() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    EXPECTED.addJoinTable(SECONDARY_TABLE_ID, JoinType.RIGHT);
    EXPECTED.addIneligibilityReason(CONTAINS_UNSUPPORTED_JOIN);
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "RIGHT JOIN mydataset.myothertable b USING(col1) "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  public void innerJoin() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    EXPECTED.addJoinTable(SECONDARY_TABLE_ID, JoinType.INNER);
    EXPECTED.clearIneligibilityReasons();
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "INNER JOIN mydataset.myothertable b USING(col1) "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  public void fullJoin() {
    final FieldSet EXPECTED =
        createFieldSet(
            MAIN_TABLE_ID, new ReferenceField("COALESCE(col1, col1)"), new ReferenceField("col1"));
    EXPECTED.addJoinTable(SECONDARY_TABLE_ID, JoinType.FULL);
    EXPECTED.addIneligibilityReason(CONTAINS_UNSUPPORTED_JOIN);
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "FULL JOIN mydataset.myothertable b USING(col1) "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  /** UNKNOWN REASON: ZetaSQL returns INNER join type on a CROSS JOIN clause */
  @Test
  public void crossJoin() {
    final FieldSet EXPECTED = createFieldSet(MAIN_TABLE_ID, new ReferenceField("col1"));
    EXPECTED.addJoinTable(SECONDARY_TABLE_ID, JoinType.INNER);
    EXPECTED.clearIneligibilityReasons();
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "CROSS JOIN mydataset.myothertable "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  public void multipleInnerJoin() {
    final FieldSet EXPECTED =
        fieldSetBuilder()
            .addRefs("col1", "col4", "col3")
            .addJoinTable(SECONDARY_TABLE_ID, JoinType.INNER)
            .addJoinTable(THIRD_TABLE_ID, JoinType.INNER)
            .build();
    String s =
        "SELECT a.col1, b.col4, c.col3 "
            + "FROM mydataset.mytable a "
            + "INNER JOIN mydataset.myothertable b USING(col1) "
            + "INNER JOIN mydataset.mythirdtable c USING(col1) "
            + "GROUP BY 1,2,3";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  @Ignore
  public void script() {
    final FieldSet EXPECTED_1 = fieldSetBuilder().addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder().addRef("col2").build();
    final String query =
        "SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1;"
            + " SELECT col2 FROM myproject.mydataset.mytable GROUP BY col2;";
    assertExpectedFieldSet(query, EXPECTED_1, EXPECTED_2);
  }

  private FieldSetBuilder fieldSetBuilder(TableId tableId) {
    return new FieldSetBuilder(tableId);
  }

  private FieldSetBuilder fieldSetBuilder() {
    return fieldSetBuilder(MAIN_TABLE_ID);
  }

  private void assertExpectedFieldSet(String statement, FieldSet... expectedFieldSets) {
    assertExpectedFieldSet(extractor.extractAll(PROJECT_ID, statement), expectedFieldSets);
  }

  private void assertExpectedFieldSet(List<FieldSet> fieldSetList, FieldSet... expectedFieldSets) {
    List<FieldSet> expected = new ArrayList<>(Arrays.asList(expectedFieldSets));
    assertEquals(expected.size(), fieldSetList.size(), "Unexpected number of fieldset");
    assertEquals(expected, fieldSetList, "Unexpected fieldset");
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
    assertExpectedFields(query, fields);
  }

  private void assertExpectedFields(String query, Field... fields) {
    final FieldSet expected = createFieldSet(fields);
    final List<FieldSet> fieldSets =
        FieldSetHelper.statementToFieldSet(PROJECT_ID, query, extractor);
    assertEquals(1, fieldSets.size(), "Only one FieldSet expected not " + fieldSets.size());
    FieldSet actual = fieldSets.get(0);
    assertEquals(expected.fields(), actual.fields(), "Unexpected fields");
  }

  private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
    Assert.assertNotNull("Actual FieldSet is null.", actual);
    Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
  }
}
