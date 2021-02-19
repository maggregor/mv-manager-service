package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.fields.Field;
import com.alwaysmart.optimizer.fields.FieldSet;

import java.util.Arrays;

public enum FieldSetHelper {

	;

	public static FieldSet statementToFieldSet(String statement, FieldSetExtract extractor) {
		FetchedQuery query = FetchedQueryFactory.createFetchedQuery(statement);
		return extractor.extract(query);
	}

	public static FieldSet createFieldSet(Field... fields) {
		return new DefaultFieldSet(Arrays.asList(fields), 0, 0);
	}

	public static FieldSet createFieldSet(long scannedBytesMb, int hits, Field... fields) {
		return new DefaultFieldSet(Arrays.asList(fields), scannedBytesMb, hits);
	}
}
