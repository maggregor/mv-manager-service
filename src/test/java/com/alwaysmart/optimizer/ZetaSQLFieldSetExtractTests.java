package com.alwaysmart.optimizer;

public class ZetaSQLFieldSetExtractTests extends FieldSetExtractTests {

	@Override
	protected FieldSetExtract createFieldSetExtract(TableMetadata metadata) {
		return new ZetaSQLFieldSetExtract(metadata);
	}
}
