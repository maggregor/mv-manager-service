package com.achilio.mvm.service.exceptions;

public class TableNotFoundException extends NotFoundException {
  public TableNotFoundException(String tableId) {
    super(String.format("Table %s not found", tableId));
  }
}
