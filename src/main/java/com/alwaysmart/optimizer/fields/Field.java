package com.alwaysmart.optimizer.fields;

/**
 * Represents a field in sql query.
 * Can be a function, an aggregate or reference to a column.
 */
public abstract class Field {

	private String name;
	private int cardinality;

	public Field(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}
}
