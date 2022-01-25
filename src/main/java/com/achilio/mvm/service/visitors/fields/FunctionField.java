package com.achilio.mvm.service.visitors.fields;

/**
 * Represents a function field.
 */
public class FunctionField extends Field {

  public FunctionField(String name) {
    super(name);
  }

  public FunctionField(String name, String alias) {
    super(name, alias);
  }

  public FunctionField(String name, String alias, float distinctValuePercent) {
    super(name, alias, distinctValuePercent);
  }
}
