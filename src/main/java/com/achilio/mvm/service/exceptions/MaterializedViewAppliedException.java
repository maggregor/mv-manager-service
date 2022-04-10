package com.achilio.mvm.service.exceptions;

public class MaterializedViewAppliedException extends IllegalArgumentException {
  public MaterializedViewAppliedException(Long id) {
    super(String.format("Materialized View %s is in APPLIED state and cannot be removed", id));
  }
}
