package com.achilio.mvm.service.exceptions;

public class ProjectAlreadyExistsException extends IllegalArgumentException {
  public ProjectAlreadyExistsException(String projectId) {
    super(String.format("Project %s already exists", projectId));
  }
}
