package com.achilio.mvm.service.extract.fields;

import java.util.HashSet;
import java.util.Set;

public enum FieldSetFactory {

	;

	public static FieldSet EMPTY_FIELD_SET = createFieldSet(new HashSet<>(), 0, 0);

	public static FieldSet createFieldSet(Set<Field> fields, long scannedBytes, int hits) {
		return new DefaultFieldSet(fields, scannedBytes, hits);
	}
}
