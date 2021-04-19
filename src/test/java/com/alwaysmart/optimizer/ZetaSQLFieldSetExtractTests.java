package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.entities.TableMetadata;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.ZetaSQLFieldSetExtract;

import java.util.List;

public class ZetaSQLFieldSetExtractTests extends FieldSetExtractTests {

	@Override
	protected FieldSetExtract createFieldSetExtract(String projectName, List<TableMetadata> tables) {
		return new ZetaSQLFieldSetExtract(projectName, tables);
	}

}
