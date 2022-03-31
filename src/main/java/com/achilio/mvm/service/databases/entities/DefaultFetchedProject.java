package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.entities.AOrganization;

public class DefaultFetchedProject implements FetchedProject {

  private String projectId;
  private String name;
  private AOrganization organization;
  private String teamName;

  public DefaultFetchedProject(String projectId, String name) {
    this.projectId = projectId;
    this.name = name;
  }

  public DefaultFetchedProject(
      String projectId, String name, AOrganization organization, String teamName) {
    this.projectId = projectId;
    this.name = name;
    this.organization = organization;
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
  public AOrganization getOrganization() {
    return this.organization;
  }

  @Override
  public String getTeamName() {
    return this.teamName;
  }
}
