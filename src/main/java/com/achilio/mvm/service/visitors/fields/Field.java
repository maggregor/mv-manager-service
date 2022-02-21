package com.achilio.mvm.service.visitors.fields;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** Represents a field in sql query. Can be a function, an aggregate or reference to a column. */
public abstract class Field {

  private static final String ALIAS_PREFIX = "a_";

  private final String expression;
  private final String alias;
  private final long countDistinct;

  public Field(String expression) {
    this(expression, ALIAS_PREFIX + Math.abs(expression.hashCode()));
  }

  @VisibleForTesting
  public Field(String expression, String alias) {
    this(expression, alias, 0);
  }

  public Field(String expression, String alias, long countDistinct) {
    this.expression = expression;
    this.alias = alias;
    this.countDistinct = countDistinct;
  }

  public long getCountDistinct() {
    return countDistinct;
  }

  public String alias() {
    return this.alias;
  }

  public String name() {
    return expression;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Field field = (Field) o;

    return new EqualsBuilder()
        .append(expression, field.expression)
        .append(alias, field.alias)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(expression).append(alias).toHashCode();
  }

  @Override
  public String toString() {
    return "['" + expression + '\'' + ", distinct=" + countDistinct + ']';
  }
}
