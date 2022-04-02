package com.achilio.mvm.service.exceptions;

public class DatasetNotFoundException extends NotFoundException {
  public DatasetNotFoundException(String datasetName) {
    super(String.format("Dataset %s not found", datasetName));
  }
}
