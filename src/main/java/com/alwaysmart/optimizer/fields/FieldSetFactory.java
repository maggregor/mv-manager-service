package com.alwaysmart.optimizer.fields;

import java.util.List;

public enum FieldSetFactory {

	;

	public static FieldSet createFieldSet(List<Field> fields, long scannedBytes, int hits) {
		return new DefaultFieldSet(fields, scannedBytes, hits);
	}
}
