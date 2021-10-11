package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.FieldSetExtract;
import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.extract.fields.*;
import com.google.zetasql.*;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.achilio.mvm.service.databases.entities.FetchedTableHelper.createFetchedTable;

@RunWith(SpringRunner.class)
public abstract class FieldSetExtractTest {

	private static final String[][] SIMPLE_TABLE_COLUMNS = new String[][]{
			{"col1", ZetaSQLType.TypeKind.TYPE_STRING.name()},
			{"col2", ZetaSQLType.TypeKind.TYPE_STRING.name()},
			{"col3", ZetaSQLType.TypeKind.TYPE_INT64.name()},
			{"col4", ZetaSQLType.TypeKind.TYPE_INT64.name()},
	};

	private FieldSetExtract extractor;

	protected abstract FieldSetExtract createFieldSetExtract(String projectName, List<FetchedTable> metadata);

	@Before
	public void before() {
		List<FetchedTable> tables = new ArrayList<>();
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
		final String query = "SELECT SUM(col3) FROM mydataset.mytable WHERE col3 < 5";
		assertContainsFields(query,
				new ReferenceField("col3"),
				new AggregateField("SUM(col3)"));
	}

	@Test
	public void whereClauseSingleReference() {
		final String query = "SELECT 'a' FROM mydataset.mytable WHERE col1 = 'a'";
		assertContainsFields(query, new ReferenceField("col1"));
	}

	@Test
	public void whereClauseMultipleReferences() {
		final String query = "SELECT 'xxx' FROM mydataset.mytable WHERE col1 = 'xxx' AND col2 = 'yyy'";
		assertContainsFields(query, new ReferenceField("col1"), new ReferenceField("col2"));
	}

	@Test
	public void discoverTablePaths() {
		FetchedQuery fetchedQuery;
		fetchedQuery = FetchedQueryFactory.createFetchedQuery("SELECT 'xxx' FROM mydataset.mytable");
		extractor.discoverTablePath(fetchedQuery);
		Assert.assertEquals("mydataset", fetchedQuery.getDatasetName());
		Assert.assertEquals("mytable", fetchedQuery.getTableName());
		fetchedQuery = FetchedQueryFactory.createFetchedQuery("SELECT COUNT(*) FROM `achilio-dev.nyc_trips.mvm_123456`");
		extractor.discoverTablePath(fetchedQuery);
		Assert.assertEquals("nyc_trips", fetchedQuery.getDatasetName());
		Assert.assertEquals("mvm_123456", fetchedQuery.getTableName());
	}

	@Test
	public void groupByMultipleReferences() {
		final String query = "SELECT col1, col2, col3 FROM mydataset.mytable GROUP BY col1, col2, col3";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new ReferenceField("col2"),
				new ReferenceField("col3"));
	}

	@Test
	public void simpleSubQueryReference() {
		String query = "SELECT col1 FROM ( SELECT col1 FROM mydataset.mytable )";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void simpleAliasSubQueryReference() {
		final String query = "SELECT myalias FROM ( SELECT col1 as myalias FROM mydataset.mytable )";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void extractAggregateWhichContainsOneColumn() {
		String query = "SELECT SUM(col3) FROM mydataset.mytable";
		assertExpectedFieldSet(query, new AggregateField("SUM(col3)"));
	}

	@Test
	public void extractAggregateWhichContainsComplexExpression() {
		String query = "SELECT SUM(col3 + col4) FROM mydataset.mytable";
		assertContainsFields(query, new AggregateField("SUM(col3 + col4)"));
		query = "SELECT SUM(col4 + col3) FROM mydataset.mytable";
		assertContainsFields(query, new AggregateField("SUM(col4 + col3)"));
	}

	@Test
	public void extractAggregatesAndReferences() {
		String query;
		query = "SELECT col1, MIN(col3) FROM mydataset.mytable GROUP BY col1";
		assertContainsFields(query,
				new ReferenceField("col1"),
				new AggregateField("MIN(col3)"));
		query = "SELECT col1, MIN(col3) FROM mydataset.mytable WHERE col1 = 'xxx' GROUP BY col1";
		assertContainsFields(query,
				new ReferenceField("col1"),
				new AggregateField("MIN(col3)"));
		query = "SELECT col1, MIN(col3) FROM mydataset.mytable WHERE col2 = 'xxx' GROUP BY col1";
		assertContainsFields(query,
				new ReferenceField("col1"),
				new ReferenceField("col2"),
				new AggregateField("MIN(col3)"));
	}

	@Test @Ignore
	// In column with small column count, we (maybe) want extract and optimize.
	public void notExtractAStarSelect() {
		String query = "SELECT * FROM mydataset.mytable";
		assertZeroFields(query);
		query = "SELECT col1, col2, col3, col4 FROM mydataset.mytable";
		assertZeroFields(query);
		query = "SELECT col3, col4 FROM mydataset.mytable WHERE col1 = 'xxx' AND col2 = 'xxx' GROUP BY col3, col4";
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
		assertExpectedFieldSet(query, new ExpressionField("col1 = (\"x\")"));
		query = "SELECT col3 + col4 FROM mydataset.mytable";
		assertExpectedFieldSet(query, new ExpressionField("col3 + col4"));
		query = "SELECT IF(col3 < 4, 'bonjour', 'aurevoir') FROM mydataset.mytable";
		assertExpectedFieldSet(query, new ExpressionField("IF(col3 < 4, \"bonjour\", \"aurevoir\")"));
		query = "SELECT CASE WHEN col1 = 'x' THEN 'a' ELSE 'b' END FROM mydataset.mytable";
		assertExpectedFieldSet(query, new ExpressionField("CASE WHEN (col1 = (\"x\")) THEN (\"a\") ELSE (\"b\") END"));
	}

	@Test //TODO: Support my-project.mydataset.mytable
	public void extractTablesWithProject() {
		final String query = "SELECT col1 FROM mydataset.mytable GROUP BY col1";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void registerFetchedTable() {
		Map<String, String> columns = new HashMap<>();
		columns.put("col", "TYPE_STRING");
		final FetchedTable fetchedTable = new DefaultFetchedTable("myproject", "myotherdataset", "mytable", columns);
		extractor.registerTable(fetchedTable);
		Assert.assertTrue(extractor.isTableRegistered(fetchedTable.getDatasetName(), fetchedTable.getTableName()));
	}

	@Test
	public void aliasWithoutGroupBy() {
		String query = "SELECT col1 as myalias FROM mydataset.mytable";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void aliasOnFunctionsShouldNotBeExtracted() {
		String query = "SELECT col1 as myalias, COUNT(*) as count FROM mydataset.mytable GROUP BY myalias";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new AggregateField("COUNT(*)"));
	}

	@Test
	// TODO: Alias support :)
	public void aliasOnFunctionsInGroupByShouldNotBeExtracted() {
		String query = "SELECT col1, CONCAT(col1, col2) as myalias, col3 FROM mydataset.mytable GROUP BY col1, myalias, col3";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new ReferenceField("col3"),
				new ExpressionField("CONCAT(col1, col2)"));
	}

	public void assertZeroFields(String query) {
		final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
		Assert.assertEquals(FieldSetFactory.EMPTY_FIELD_SET, actual);
		Assert.assertTrue("Actual FieldSet should be empty", actual.fields().isEmpty());
	}

	private void assertContainsFields(String query, Field...fields) {
		final FieldSet expected = FieldSetHelper.createFieldSet(fields);
		final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
		for (Field field : expected.fields()) {
			Assert.assertTrue(
					String.format("One field is missing: %s.\nActual fields: %s", field.name(), actual),
					actual.fields().contains(field)
			);
		}
	}

	private void assertExpectedFieldSet(String query, Field...fields) {
		final FieldSet expected = FieldSetHelper.createFieldSet(fields);
		final FieldSet actual = FieldSetHelper.statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
		Assert.assertNotNull("Actual FieldSet is null.", actual);
		Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
	}

}
