package com.alwaysmart.optimizer.fields;

/**
 * Represents an aggregate field.
 */
public class AggregateField extends Field {

	public AggregateField(String name) {
		super(name);
		// Cardinality of an aggregate is always 1.
		this.setCardinality(1);
	}
}
