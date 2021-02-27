package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.AggregateField;
import com.alwaysmart.optimizer.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.fields.Field;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.ReferenceField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.alwaysmart.optimizer.FieldSetHelper.createFieldSet;
import static com.alwaysmart.optimizer.FieldSetHelper.statementToFieldSet;
import static com.alwaysmart.optimizer.TableMetadataHelper.createTableMetadata;

@RunWith(MockitoJUnitRunner.class)
public abstract class FieldSetExtractTests {

	private static final String[][] SIMPLE_TABLE_COLUMNS = new String[][]{
			{"col1", "STRING"}, {"col2", "STRING"}, {"col3", "INT64"}, {"col4", "INT64"}
	};

	private FieldSetExtract extractor;
	private TableMetadata simpleTable;

	protected abstract FieldSetExtract createFieldSetExtract(TableMetadata metadata);

	@Before
	public void before() {
		this.simpleTable = createTableMetadata("default", "mytable", SIMPLE_TABLE_COLUMNS);
		this.extractor = createFieldSetExtract(simpleTable);
	}

	@Test
	public void singleReference() {
		final String query = "SELECT col1 FROM mytable";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void multipleReferences() {
		final String query = "SELECT col1, col2 FROM mytable";
		assertExpectedFieldSet(query, new ReferenceField("col1"), new ReferenceField("col2"));
	}

	@Test
	public void whereClauseSingleReference() {
		final String query = "SELECT 'a' FROM mytable WHERE col1 = 'a'";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void whereClauseMultipleReferences() {
		final String query = "SELECT 'xxx' FROM mytable WHERE col1 = 'xxx' AND col2 = 'yyy'";
		assertExpectedFieldSet(query, new ReferenceField("col1"), new ReferenceField("col2"));
	}

	@Test
	public void groupByMultipleReferences() {
		final String query = "SELECT col1, col2, col3 FROM mytable GROUP BY col1, col2, col3";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new ReferenceField("col2"),
				new ReferenceField("col3"));
	}

	@Test
	public void simpleSubQueryReference() {
		String query = "SELECT col1 FROM ( SELECT col1 FROM mytable )";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void simpleAliasSubQueryReference() {
		final String query = "SELECT myalias FROM ( SELECT col1 as myalias FROM mytable )";
		assertExpectedFieldSet(query, new ReferenceField("col1"));
	}

	@Test
	public void extractAggregateWhichContainsOneColumn() {
		String query = "SELECT SUM(col3) FROM mytable";
		assertExpectedFieldSet(query, new AggregateField("SUM(col3)"));
	}

	@Test
	public void extractAggregateWhichContainsComplexExpression() {
		String query = "SELECT SUM(col3 + col4) FROM mytable";
		assertExpectedFieldSet(query, new AggregateField("SUM(col3 + col4)"));
		query = "SELECT SUM(col4 + col3) FROM mytable";
		assertExpectedFieldSet(query, new AggregateField("SUM(col4 + col3)"));
	}

	@Test
	public void extractAggregatesAndReferences() {
		String query;
		query = "SELECT col1, MIN(col3) FROM mytable GROUP BY col1";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new AggregateField("MIN(col3)"));
		query = "SELECT col1, MIN(col3) FROM mytable WHERE col1 = 'xxx' GROUP BY col1";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new AggregateField("MIN(col3)"));
		query = "SELECT col1, MIN(col3) FROM mytable WHERE col2 = 'xxx' GROUP BY col1";
		assertExpectedFieldSet(query,
				new ReferenceField("col1"),
				new ReferenceField("col2"),
				new AggregateField("MIN(col3)"));
	}

	@Test
	public void notExtractAStarSelect() {
		String query = "SELECT * FROM mytable";
		assertZeroFields(query);
		query = "SELECT col1, col2, col3, col4 FROM mytable";
		assertZeroFields(query);
		query = "SELECT col3, col4 FROM mytable WHERE col1 = 'xxx' AND col2 = 'xxx' GROUP BY col3, col4";
		assertZeroFields(query);
		query = "SELECT col1, col2, col3, col4 FROM mytable GROUP BY col1, col2, col3, col4";
		assertZeroFields(query);
		query = "SELECT * FROM (SELECT * FROM mytable)";
		assertZeroFields(query);
		query = "SELECT * FROM (SELECT * FROM (SELECT col1, col2, col3, col4 FROM mytable))";
		assertZeroFields(query);
	}

	public void assertZeroFields(String query) {
		final FieldSet actual = statementToFieldSet(query, extractor);
		Assert.assertEquals(new DefaultFieldSet(), actual);
		Assert.assertTrue("Actual FieldSet should be empty", actual.fields().isEmpty());
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
