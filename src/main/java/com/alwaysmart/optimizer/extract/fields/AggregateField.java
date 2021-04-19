package com.alwaysmart.optimizer.extract.fields;

/**
 * Represents an aggregate field.
 */
public class AggregateField extends Field {

	public AggregateField(String name) {
		// Cardinality of an aggregate is always 1.
		// Because of no-effect on table cardinality with an aggregate field.
		super(name, 1);
	}
}
