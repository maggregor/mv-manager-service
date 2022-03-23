package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.Project;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectResponse {

  @JsonProperty("projectId")
  private final String projectId;

  @JsonProperty("projectName")
  private String projectName;

  @JsonProperty("username")
  private String username;

  @JsonProperty("mvMaxPerTable")
  private Integer mvMaxPerTable;

  @JsonProperty("analysisTimeframe")
  private Integer analysisTimeframe;

  @JsonProperty("activated")
  private Boolean activated;

  @JsonProperty("automatic")
  private Boolean automatic;

  @JsonProperty("stripeSubscriptionId")
  private String stripeSubscriptionId;

  @JsonProperty("organization")
  private AOrganization organization;

  public ProjectResponse(String projectId, String projectName) {
    this.projectId = projectId;
    this.projectName = projectName;
  }

  public ProjectResponse(String projectName, Project project) {
    this(project.getProjectId(), projectName);
    this.activated = project.isActivated();
    this.automatic = project.isAutomatic();
    this.username = project.getUsername();
    this.mvMaxPerTable = project.getMvMaxPerTable();
    this.analysisTimeframe = project.getAnalysisTimeframe();
    this.stripeSubscriptionId = project.getStripeSubscriptionId();
    this.organization = project.getOrganization();
  }

  public ProjectResponse(Project project) {
    this.projectId = project.getProjectId();
    this.projectName = project.getProjectName();
    this.activated = project.isActivated();
    this.automatic = project.isAutomatic();
    this.username = project.getUsername();
    this.mvMaxPerTable = project.getMvMaxPerTable();
    this.analysisTimeframe = project.getAnalysisTimeframe();
    this.stripeSubscriptionId = project.getStripeSubscriptionId();
    this.organization = project.getOrganization();
  }
}
