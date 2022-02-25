package com.achilio.mvm.service.exceptions;

public class ProjectNotFoundException extends RuntimeException {

  public ProjectNotFoundException(String projectId) {
    super(String.format("Project %s not found", projectId));
  }
}
