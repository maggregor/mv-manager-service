package com.alwaysmart.optimizer.extract.fields;

/**
 * Represents a reference field.
 */
public class ReferenceField extends Field {

	public ReferenceField(String name) {
		super(name);
	}

	public ReferenceField(String name, int cardinality) {
		super(name, cardinality);
	}

}
