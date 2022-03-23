package com.achilio.mvm.service.databases.entities;

public class DefaultFetchedProject implements FetchedProject {

  private String projectId;
  private String name;
  private String organizationId;

  public DefaultFetchedProject(String projectId, String name) {
    this.projectId = projectId;
    this.name = name;
  }

  public DefaultFetchedProject(String projectId, String name, String organizationId) {
    this.projectId = projectId;
    this.name = name;
    this.organizationId = organizationId;
  }

  @Override
  public String getProjectId() {
    return projectId;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
