package com.achilio.mvm.service.exceptions;

public class ConnectionNotFoundException extends IllegalArgumentException {

  public ConnectionNotFoundException(Long connectionId) {
    super(String.format("Connection %s not found", connectionId));
  }
}
