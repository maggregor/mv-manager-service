package com.achilio.mvm.service.extract.fields;

/**
 * Represents an expression field.
 */
public class ExpressionField extends Field {

	public ExpressionField(String name) {
		super(name);
	}

	public ExpressionField(String name, int cardinality) {
		super(name, cardinality);
	}

}
