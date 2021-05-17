package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.MaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.cloud.bigquery.TableId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
public class BigQueryMaterializedViewStatementBuilderTests {

	private final BigQueryMaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();

	@Test @Ignore
	public void testTableReferenceSerializationWithoutProject() {
		TableId tableId = TableId.of("mydataset", "mytable");
		Throwable exception = assertThrows(IllegalArgumentException.class, () -> builder.buildTableReference(tableId));
		assertEquals("Project name is empty or null", exception.getMessage());
		;
	}

	@Test
	public void testTableReferenceSerialization() {
		TableId tableId = TableId.of("myproject","mydataset", "mytable");
		Assert.assertEquals("`myproject`.`mydataset`.`mytable`", builder.buildTableReference(tableId));
	}

	private void assertStatementFromFieldSet(FieldSet fieldSet, String expected) {
		MaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();
		String statement = builder.build(fieldSet);
		Assert.assertEquals(statement, expected);
	}
}
