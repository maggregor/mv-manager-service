package com.achilio.mvm.service.exceptions;

public class InvalidSettingsException extends IllegalArgumentException {
  public InvalidSettingsException(String errorMsg) {
    super(errorMsg);
  }
}
