package com.achilio.mvm.service.exceptions;

public class ConnectionInUseException extends IllegalArgumentException {
  public ConnectionInUseException(String exceptionMessage) {
    super(exceptionMessage);
  }
}
