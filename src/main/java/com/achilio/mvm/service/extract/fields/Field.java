package com.achilio.mvm.service.extract.fields;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Represents a field in sql query.
 * Can be a function, an aggregate or reference to a column.
 */
public abstract class Field {

	private String expression;
	private int cardinality;
	private String alias;

	Field(String expression) {
		this(expression, 1);
	}

	Field(String expression, int cardinality) {
		this.expression = expression;
		this.cardinality = cardinality;
	}

	public boolean hasAlias() {
		return !StringUtils.isEmpty(alias);
	}

	public String alias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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
		return cardinality == field.cardinality && expression.equals(field.expression) && Objects.equals(alias, field.alias);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression, cardinality, alias);
	}

	@Override
	public String toString() {
		return "Field{" +
				"class='" + getClass().getSimpleName() + '\'' +
				"expression='" + expression + '\'' +
				", cardinality=" + cardinality +
				", alias='" + alias + '\'' +
				'}';
	}
}
