package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedQueryFactory;
import com.alwaysmart.optimizer.extract.FieldSetExtract;
import com.alwaysmart.optimizer.extract.fields.DefaultFieldSet;
import com.alwaysmart.optimizer.extract.fields.Field;
import com.alwaysmart.optimizer.extract.fields.FieldSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FieldSetHelper {

	;

	public static FieldSet statementToFieldSet(String statement, FieldSetExtract extractor) {
		FetchedQuery query = FetchedQueryFactory.createFetchedQuery(statement);
		return extractor.extract(query);
	}

	public static FieldSet createFieldSet(Field... fields) {
		return createFieldSet(0, 0, new HashSet<>(Arrays.asList(fields)));
	}

	public static FieldSet createFieldSet(long scannedBytesMb, int hits, Field... fields) {
		return createFieldSet(scannedBytesMb, hits, new HashSet<>(Arrays.asList(fields)));
	}

	public static FieldSet createFieldSet(long scannedBytesMb, int hits, Set<Field> fields) {
		return new DefaultFieldSet(fields, scannedBytesMb, hits);
	}
}
