package com.alwaysmart.optimizer;

public class AggregateField extends Field {

	public AggregateField(String name) {
		super(name);
		this.setCardinality(1);
	}

}
