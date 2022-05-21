package com.achilio.mvm.service.visitors.fields;

import java.util.Objects;

/**
 * Represents a field in sql query. Can be a function, an aggregate or reference to a column.
 */
@Deprecated
public abstract class Field {

  private static final String ALIAS_PREFIX = "a_";

  private final String expression;
  private final String alias;

  public Field(String expression) {
    this(expression, ALIAS_PREFIX + Math.abs(expression.hashCode()));
  }

  public Field(String expression, String alias) {
    this.expression = expression;
    this.alias = alias;
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
    if (!(o instanceof Field)) {
      return false;
    }

    Field field = (Field) o;

    if (!Objects.equals(expression, field.expression)) {
      return false;
    }
    return Objects.equals(alias, field.alias);
  }

  @Override
  public int hashCode() {
    int result = expression != null ? expression.hashCode() : 0;
    result = 31 * result + (alias != null ? alias.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Field{" + "expression='" + expression + '\'' + ", alias='" + alias + '\'' + '}';
  }
}
