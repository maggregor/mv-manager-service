package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.extract.FieldSetExtract;
import com.achilio.mvm.service.extract.ZetaSQLFieldSetExtract;

import java.util.List;

public class ZetaSQLFieldSetExtractTest extends FieldSetExtractTest {

	@Override
	protected FieldSetExtract createFieldSetExtract(String projectName, List<FetchedTable> tables) {
		return new ZetaSQLFieldSetExtract(projectName, tables);
	}

}
