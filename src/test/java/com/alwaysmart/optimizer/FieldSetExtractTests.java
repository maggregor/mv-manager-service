package com.alwaysmart.optimizer;

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
			{"col1", "STRING"}, {"col2", "STRING"}, {"col3", "NUMERIC"}, {"col4", "NUMERIC"}
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
		final FieldSet expected = createFieldSet(new ReferenceField("col1"));
		final FieldSet actual = statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	@Test
	public void multipleReferences() {
		final String query = "SELECT col1, col2 FROM mytable";
		final FieldSet expected = createFieldSet(
				new ReferenceField("col1"),
				new ReferenceField("col2")
		);
		final FieldSet actual = statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	@Test
	public void whereClauseSingleReference() {
		final String query = "SELECT 'xxx' FROM mytable WHERE col2 = 'xxx'";
		final FieldSet expected = createFieldSet(
				new ReferenceField("col2")
		);
		final FieldSet actual = statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	@Test
	public void whereClauseMultipleReferences() {
		final String query = "SELECT 'xxx' FROM mytable WHERE col1 = 'xxx' AND col2 = 'yyy'";
		final FieldSet expected = createFieldSet(
				new ReferenceField("col1"),
				new ReferenceField("col2")
		);
		final FieldSet actual = statementToFieldSet(query, extractor);
		assertExpectedFieldSet(expected, actual);
	}

	private void assertExpectedFieldSet(FieldSet expected, FieldSet actual) {
		Assert.assertNotNull("Actual FieldSet is null.", actual);
		Assert.assertEquals("Actual FieldSet wasn't expected.", expected, actual);
	}

}
