package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.MaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.cloud.bigquery.TableId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryMaterializedViewStatementBuilderTests {

	private final BigQueryMaterializedViewStatementBuilder builder = new BigQueryMaterializedViewStatementBuilder();

	/**
	 * https://cloud.google.com/bigquery/docs/schemas#column_names
	 */
	@Test
	public void checkGenerationAliasRespectRules() {
		List<String> aliases = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			String alias = builder.generateRandomAlias();
			Assert.assertFalse( "Duplicate alias: " + alias, aliases.contains(alias));
			Assert.assertNotNull(alias);
			Assert.assertTrue(alias.length() > 0 && alias.length() <= 300);
			Assert.assertFalse(alias.contains("-"));
			Assert.assertTrue("First char should be a letter: " + alias,Character.isLetter(alias.charAt(0)));
			aliases.add(alias);
		}
	}

	@Test
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
