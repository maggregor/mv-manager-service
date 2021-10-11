package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.extract.fields.DefaultFieldSet;
import com.achilio.mvm.service.extract.fields.FieldSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
public class BigQueryMaterializedViewStatementBuilderTest {

	private final BigQueryMaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();

	@Test
	@Ignore
	public void testTableReferenceSerializationWithoutProject() {
		FieldSet fieldSet = new DefaultFieldSet();
		fieldSet.setDataset("mydataset");
		fieldSet.setTable("mytable");
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> builder.buildTableReference(fieldSet));
		assertEquals("Project name is empty or null", exception.getMessage());
		;
	}

	@Test
	public void testTableReferenceSerialization() {
		FieldSet fieldSet = new DefaultFieldSet();
		fieldSet.setProjectId("myproject");
		fieldSet.setDataset("mydataset");
		fieldSet.setTable("mytable");
		Assert.assertEquals("`myproject`.`mydataset`.`mytable`", builder.buildTableReference(fieldSet));
	}

	private void assertStatementFromFieldSet(FieldSet fieldSet, String expected) {
		MaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();
		String statement = builder.build(fieldSet);
		Assert.assertEquals(statement, expected);
	}
}
