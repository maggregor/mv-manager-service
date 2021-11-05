package com.achilio.mvm.service.extract.fields;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a field in sql query. Can be a function, an aggregate or reference to a column.
 */
public abstract class Field {

  private static final String ALIAS_PREFIX = "a_";

  private final String expression;
  private final int cardinality;
  private final String alias;

  public Field(String expression) {
    this(expression, 1);
  }

  public Field(String expression, int cardinality) {
    this(expression, ALIAS_PREFIX + Math.abs(expression.hashCode()), cardinality);
  }

  @VisibleForTesting
  public Field(String expression, String alias) {
    this(expression, alias, 1);
  }

  public Field(String expression, String alias, int cardinality) {
    this.expression = expression;
    this.alias = alias;
    this.cardinality = cardinality;
  }

  public boolean hasAlias() {
    return !StringUtils.isEmpty(alias);
  }

  public String alias() {
    return this.alias;
  }

  public String name() {
    return expression;
  }

  public int cardinality() {
    return this.cardinality;
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
    return cardinality == field.cardinality
        && expression.equals(field.expression)
        && Objects.equals(alias, field.alias);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expression, cardinality, alias);
  }

  @Override
  public String toString() {
    return "Field{"
        + "class='"
        + getClass().getSimpleName()
        + '\''
        + "expression='"
        + expression
        + '\''
        + ", cardinality="
        + cardinality
        + ", alias='"
        + alias
        + '\''
        + '}';
  }
}
