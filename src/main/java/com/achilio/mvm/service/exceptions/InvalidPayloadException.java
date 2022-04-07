package com.achilio.mvm.service.exceptions;

public class InvalidPayloadException extends IllegalArgumentException {
  public InvalidPayloadException() {
    super("Invalid payload");
  }
}
