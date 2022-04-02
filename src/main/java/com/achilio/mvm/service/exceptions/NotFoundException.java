package com.achilio.mvm.service.exceptions;

public class NotFoundException extends IllegalArgumentException {
  public NotFoundException(String exceptionMessage) {
    super(exceptionMessage);
  }
}
