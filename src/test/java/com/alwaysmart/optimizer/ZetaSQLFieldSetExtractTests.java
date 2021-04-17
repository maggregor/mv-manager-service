package com.alwaysmart.optimizer;

import java.util.List;

public class ZetaSQLFieldSetExtractTests extends FieldSetExtractTests {


	@Override
	protected FieldSetExtract createFieldSetExtract(List<TableMetadata> tables) {
		return new ZetaSQLFieldSetExtract(tables);
	}

}
