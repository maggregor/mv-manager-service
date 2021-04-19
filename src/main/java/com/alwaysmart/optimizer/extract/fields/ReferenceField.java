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

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
