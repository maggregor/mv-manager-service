package com.alwaysmart.optimizer.extract.fields;

import java.util.Objects;

/**
 * Represents a field in sql query.
 * Can be a function, an aggregate or reference to a column.
 */
public abstract class Field {

	private String expression;
	private int cardinality;

	Field(String expression) {
		this(expression, 1);
	}

	Field(String expression, int cardinality) {
		this.expression = expression;
		this.cardinality = cardinality;
	}

	public String name() {
		return expression;
	}

	public int cardinality() {
		return this.cardinality;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Field field = (Field) o;
		return Objects.equals(expression, field.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression, cardinality);
	}

	@Override
	public String toString() {
		return "Field{" +
				"expression='" + expression + '\'' +
				", cardinality=" + cardinality +
				'}';
	}
}
