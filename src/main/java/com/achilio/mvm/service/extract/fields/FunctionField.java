package com.achilio.mvm.service.extract.fields;

/** Represents a function field. */
public class FunctionField extends Field {

  public FunctionField(String name) {
    super(name);
  }

  public FunctionField(String name, int cardinality) {
    super(name, cardinality);
  }
}
