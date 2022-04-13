package com.achilio.mvm.service.exceptions;

public class MaterializedViewNotFoundException extends NotFoundException {
  public MaterializedViewNotFoundException(Long mvId) {
    super(String.format("Materialized View %s not found", mvId));
  }
}
