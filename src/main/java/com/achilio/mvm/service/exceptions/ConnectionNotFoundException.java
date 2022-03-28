package com.achilio.mvm.service.exceptions;

public class ConnectionNotFoundException extends RuntimeException {

  public ConnectionNotFoundException(Object connectionId) {
    super(String.format("Connection %s not found", connectionId.toString()));
  }
}
