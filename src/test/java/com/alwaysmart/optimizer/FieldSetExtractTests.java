package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.fields.AggregateField;
import com.alwaysmart.optimizer.extract.fields.Field;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.alwaysmart.optimizer.extract.fields.FieldSetFactory;
import com.alwaysmart.optimizer.extract.fields.ReferenceField;
import com.google.zetasql.ZetaSQLType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.alwaysmart.optimizer.FieldSetHelper.createFieldSet;
import static com.alwaysmart.optimizer.FieldSetHelper.statementToFieldSet;
import static com.alwaysmart.optimizer.databases.entities.FetchedTableHelper.createFetchedTable;

@RunWith(SpringRunner.class)
public abstract class FieldSetExtractTests {

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
		assertContainsFields(query, new ReferenceField("col3"), new AggregateField("SUM(col3)"));
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
	public void extractFunction() {
		String query = "SELECT col1 = 'x' FROM mydataset.mytable";
		assertExpectedFieldSet(query, new AggregateField("col1 = (\"x\")"));
		query = "SELECT col3 + col4 FROM mydataset.mytable";
		assertExpectedFieldSet(query, new AggregateField("col3 + col4"));
		query = "SELECT IF(col3 < 4, 'bonjour', 'aurevoir') FROM mydataset.mytable";
		assertExpectedFieldSet(query, new AggregateField("IF(col3 < 4, \"bonjour\", \"aurevoir\")"), new AggregateField("col3 < 4"));
		query = "SELECT CASE WHEN col1 = 'x' THEN 'a' ELSE 'b' END FROM mydataset.mytable";
		assertExpectedFieldSet(query, new AggregateField("CASE WHEN (col1 = (\"x\")) THEN (\"a\") ELSE (\"b\") END"), new AggregateField("col1 = (\"x\")"));
	}

	@Test //TODO: Support my-project.mydataset.mytable
	public void extractTablesWithProject() {
		final String query = "SELECT col1 FROM my-project.mydataset.mytable";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	public void assertZeroFields(String query) {
		final FieldSet actual = statementToFieldSet(query, extractor);
		Assert.assertEquals(FieldSetFactory.EMPTY_FIELD_SET, actual);
		Assert.assertTrue("Actual FieldSet should be empty", actual.fields().isEmpty());
	}

	private void assertContainsFields(String query, Field...fields) {
		final FieldSet expected = createFieldSet(fields);
		final FieldSet actual = statementToFieldSet(query, extractor);
		for (Field field : expected.fields()) {
			Assert.assertTrue("One field is missing: " + field.name(), actual.fields().contains(field));
		}
	}

	private void assertExpectedFieldSet(String query, Field...fields) {
		final FieldSet expected = createFieldSet(fields);
		final FieldSet actual = statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
		Assert.assertNotNull("Actual FieldSet is null.", actual);
		Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
	}

}
