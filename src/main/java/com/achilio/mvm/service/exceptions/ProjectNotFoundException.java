package com.achilio.mvm.service.exceptions;

public class ProjectNotFoundException extends RuntimeException {

  public ProjectNotFoundException(String errorMsg) {
    super(errorMsg);
  }
}
