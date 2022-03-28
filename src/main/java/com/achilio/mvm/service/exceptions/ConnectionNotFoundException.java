package com.achilio.mvm.service.exceptions;

public class ConnectionNotFoundException extends RuntimeException {

  public ConnectionNotFoundException(String connectionId) {
    super(String.format("Connection %s not found", connectionId));
  }
}
