package com.achilio.mvm.service.databases.entities;

public class DefaultFetchedProject implements FetchedProject {

  private String projectId;
  private String name;
  private String teamName;

  public DefaultFetchedProject(String projectId, String name) {
    this.projectId = projectId;
    this.name = name;
  }

  public DefaultFetchedProject(String projectId, String name, String teamName) {
    this.projectId = projectId;
    this.name = name;
    this.teamName = teamName;
  }

  @Override
  public String getProjectId() {
    return projectId;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getTeamName() {
    return this.teamName;
  }
}
