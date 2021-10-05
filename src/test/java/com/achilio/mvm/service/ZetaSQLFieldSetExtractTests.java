package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.FieldSetExtract;
import com.achilio.mvm.service.extract.ZetaSQLFieldSetExtract;
import com.achilio.mvm.service.databases.entities.FetchedTable;

import java.util.List;

public class ZetaSQLFieldSetExtractTests extends FieldSetExtractTests {

	@Override
	protected FieldSetExtract createFieldSetExtract(String projectName, List<FetchedTable> tables) {
		return new ZetaSQLFieldSetExtract(projectName, tables);
	}

}
