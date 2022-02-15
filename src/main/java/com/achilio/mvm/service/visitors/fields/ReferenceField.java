package com.achilio.mvm.service.visitors.fields;

/** Represents a reference field. */
public class ReferenceField extends Field {

  public ReferenceField(String name) {
    super(name);
  }

  public ReferenceField(String name, String alias) {
    super(name, alias);
  }

  public ReferenceField(String name, String alias, float distinctValuePercent) {
    super(name, alias, distinctValuePercent);
  }
}
