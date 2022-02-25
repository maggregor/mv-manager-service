package com.achilio.mvm.service.visitors.fields;

/** Represents an aggregate field. */
public class AggregateField extends FunctionField {

  public AggregateField(String name) {
    super(name);
  }

  public AggregateField(String name, String alias) {
    super(name, alias);
  }
}
