package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.ZetaSQLFieldSetExtract;

import java.util.List;

public class ZetaSQLFieldSetExtractTests extends FieldSetExtractTests {

	@Override
	protected FieldSetExtract createFieldSetExtract(String projectName, List<FetchedTable> tables) {
		return new ZetaSQLFieldSetExtract(projectName, tables);
	}

}
