package com.alwaysmart.optimizer.fields;

import java.util.Objects;

/**
 * Represents a field in sql query.
 * Can be a function, an aggregate or reference to a column.
 */
public abstract class Field {

	private String expression;
	private int cardinality;

	public Field(String expression) {
		this.expression = expression;
	}

	public String getName() {
		return expression;
	}

	public void setName(String name) {
		this.expression = name;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Field field = (Field) o;
		return cardinality == field.cardinality &&
				Objects.equals(expression, field.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression, cardinality);
	}
}
