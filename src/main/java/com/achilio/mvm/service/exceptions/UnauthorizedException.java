package com.achilio.mvm.service.exceptions;

public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String errorMsg) {
    super(errorMsg);
  }
}
