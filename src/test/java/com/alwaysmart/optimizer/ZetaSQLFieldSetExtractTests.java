package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.ReferenceField;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZetaSQLFieldSetExtractTests {


	private FieldSetExtract extractor = new ZetaSQLFieldSetExtract();

	@Test
	public void simpleExtract() {
		TableMetadata metadata = new TableMetadata("default", "mytable");
		metadata.addColumn("col1", "STRING");
		FetchedQuery query = new FetchedQuery("SELECT col1 FROM mytable");
		Assert.assertEquals(extractor.extract(query, metadata), new FieldSet(new ReferenceField("col1")));
	}

}
