package com.achilio.mvm.service;

import static com.achilio.mvm.service.FieldSetHelper.createFieldSet;
import static com.achilio.mvm.service.MockHelper.columnMock;
import static com.achilio.mvm.service.MockHelper.queryMock;
import static com.achilio.mvm.service.MockHelper.tableMock;
import static com.achilio.mvm.service.visitors.fieldsets.FieldSetIneligibilityReason.CONTAINS_UNSUPPORTED_JOIN;
import static com.achilio.mvm.service.visitors.fieldsets.FieldSetIneligibilityReason.DOES_NOT_CONTAIN_A_GROUP_BY;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetExtract;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class FieldSetExtractTest {

  private static final String PROJECT_ID = "myproject";
  private static final ATableId MAIN_TABLE_ID = ATableId.of(PROJECT_ID, "mydataset", "mytable");
  private static final ATableId SECONDARY_TABLE_ID =
      ATableId.of(PROJECT_ID, "mydataset", "myothertable");
  private static final ATableId THIRD_TABLE_ID =
      ATableId.of(PROJECT_ID, "mydataset", "mythirdtable");
  private static final ATableId FOURTH_TABLE_ID =
      ATableId.of(PROJECT_ID, "myotherdataset", "myfourthtable");
  private final ATable MAIN_TABLE = simpleTableMock(MAIN_TABLE_ID);
  private final ATable SECONDARY_TABLE = simpleTableMock(SECONDARY_TABLE_ID);
  private final ATable THIRD_TABLE = simpleTableMock(THIRD_TABLE_ID);
  private final ATable FOURTH_TABLE = simpleTableMock(FOURTH_TABLE_ID);
  private FieldSetExtract extractor;

  private ATable simpleTableMock(ATableId tableId) {
    List<AColumn> columns = new ArrayList<>();
    columns.add(columnMock("col1", "TYPE_STRING"));
    columns.add(columnMock("col2", "TYPE_STRING"));
    columns.add(columnMock("col3", "TYPE_INT64"));
    columns.add(columnMock("col4", "TYPE_INT64"));
    columns.add(columnMock("ts", "TYPE_TIMESTAMP"));
    return tableMock(tableId, columns);
  }

  protected abstract FieldSetExtract createFieldSetExtract(Set<ATable> tables);

  @Before
  public void setUp() {
    Set<ATable> t = Sets.newLinkedHashSet(MAIN_TABLE, SECONDARY_TABLE, THIRD_TABLE, FOURTH_TABLE);
    this.extractor = createFieldSetExtract(t);
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
    final FieldSet EXPECTED = fieldSetBuilder().addRef("col3").build();
    String q = "WITH a AS (SELECT col3 FROM mydataset.mytable GROUP BY 1) SELECT SUM(col3) FROM a";
    assertExpectedFieldSet(q, EXPECTED);
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
  @Ignore
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
  @Ignore
  public void crossJoin() {
    final FieldSet EXPECTED =
        fieldSetBuilder().addRef("col1").addJoinTable(SECONDARY_TABLE_ID, JoinType.INNER).build();
    EXPECTED.clearIneligibilityReasons();
    String s =
        "SELECT a.col1 "
            + "FROM mydataset.mytable a "
            + "CROSS JOIN mydataset.myothertable "
            + "GROUP BY 1";
    assertExpectedFieldSet(s, EXPECTED);
  }

  @Test
  @Ignore
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
  public void script() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    final StringJoiner script = new StringJoiner(";");
    script.add("SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1");
    script.add("SELECT col2 FROM myproject.mydataset.myothertable GROUP BY col2");
    assertExpectedFieldSet(script.toString(), EXPECTED_1, EXPECTED_2);
  }

  @Test
  public void queryWithInlineComments() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    final StringJoiner script = new StringJoiner("\n");
    script.add("-- I am a comment");
    script.add("SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1;");
    script.add("   -- I am a comment and I start with a blank");
    script.add("SELECT col2 FROM myproject.mydataset.myothertable GROUP BY col2;");
    script.add("\n-- I am a comment");
    assertExpectedFieldSet(script.toString(), EXPECTED_1, EXPECTED_2);
  }

  @Test
  public void queryWithMultipleLinesComments() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    final StringJoiner script = new StringJoiner("\n");
    script.add("/* I am a comment */");
    script.add("SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1;");
    script.add("/* \n\n\n I am a comment and I start with a blank \n\n\n*/");
    script.add("SELECT col2 FROM myproject.mydataset.myothertable GROUP BY col2;");
    script.add("/* I am a comment */");
    assertExpectedFieldSet(script.toString(), EXPECTED_1, EXPECTED_2);
  }

  @Test
  public void queryInlineCommentWithoutSemicolonAtTheEndOfStatement() {
    final StringJoiner script = new StringJoiner("\n");
    script.add("-- I am a comment");
    assertZeroFields(script.toString());
  }

  @Test
  public void multipleExtracts() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    AQuery q1 = queryMock("SELECT col1 FROM myproject.mydataset.mytable GROUP BY 1");
    AQuery q2 = queryMock("SELECT col2 FROM mydataset.myothertable GROUP BY 1");
    assertExpectedFieldSet(extractor.extractAll(Arrays.asList(q1, q2)), EXPECTED_1, EXPECTED_2);
  }

  @Test
  public void scriptIgnoreNotQueryStatement() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    String script =
        "SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1;"
            + "INSERT INTO myproject.mydataset.mytable (col1) VALUES ('');"
            + "SELECT col2 FROM myproject.mydataset.myothertable GROUP BY col2;";
    assertExpectedFieldSet(script, EXPECTED_1, EXPECTED_2);
  }

  @Test(timeout = 10000)
  public void dontBlockWhenResolvingFail() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    String script =
        "SELECT col1 FROM myproject.mydataset.mytable GROUP BY col1;"
            + "blablablabla;"
            + "SELECT col2 FROM myproject.mydataset.myothertable GROUP BY col2;";
    // Just first query was resolved
    assertExpectedFieldSet(script, EXPECTED_1);
  }

  @Test
  public void defaultDataset() {
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    String query = "SELECT col1 FROM mytable GROUP BY col1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_1);
  }

  @Test
  public void multipleDefaultDatasets() {
    String query;
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(SECONDARY_TABLE_ID).addRef("col2").build();
    final FieldSet EXPECTED_3 = fieldSetBuilder(THIRD_TABLE_ID).addAgg("SUM(col3)").build();
    query = "SELECT col1 FROM mytable GROUP BY col1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_1);
    query = "SELECT col1 FROM mytable GROUP BY col1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_1);
    query = "SELECT col2 FROM myothertable GROUP BY col2";
    assertExpectedFieldSet(query, SECONDARY_TABLE_ID.getDatasetName(), EXPECTED_2);
    query = "SELECT col2, (SELECT SUM(col3) FROM mythirdtable) FROM myothertable GROUP BY 1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_2, EXPECTED_3);
    query =
        "SELECT col2, (SELECT SUM(col3) FROM mydataset.mythirdtable) FROM myothertable GROUP BY 1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_2, EXPECTED_3);
  }

  @Test
  public void multipleDefaultWithDifferentDatasets() {
    String query;
    final FieldSet EXPECTED_1 = fieldSetBuilder(MAIN_TABLE_ID).addRef("col1").build();
    final FieldSet EXPECTED_2 = fieldSetBuilder(FOURTH_TABLE_ID).addAgg("SUM(col3)").build();
    query =
        "SELECT col1, (SELECT SUM(col3) FROM myotherdataset.myfourthtable) as b FROM mytable GROUP BY col1";
    assertExpectedFieldSet(query, MAIN_TABLE_ID.getDatasetName(), EXPECTED_1, EXPECTED_2);
  }

  private FieldSetBuilder fieldSetBuilder(ATableId tableId) {
    return new FieldSetBuilder(tableId);
  }

  private FieldSetBuilder fieldSetBuilder() {
    return fieldSetBuilder(MAIN_TABLE_ID);
  }

  private void assertExpectedFieldSet(String statement, FieldSet... expectedFieldSets) {
    assertExpectedFieldSet(statement, null, expectedFieldSets);
  }

  private void assertExpectedFieldSet(
      String statement, String defaultDataset, FieldSet... expectedFieldSets) {
    AQuery query = queryMock(statement);
    when(query.hasDefaultDataset()).thenReturn(true);
    when(query.getDefaultDataset()).thenReturn(defaultDataset);
    assertExpectedFieldSet(query, expectedFieldSets);
  }

  private void assertExpectedFieldSet(AQuery query, FieldSet... expectedFieldSets) {
    assertExpectedFieldSet(extractor.extractAll(query), expectedFieldSets);
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
}
