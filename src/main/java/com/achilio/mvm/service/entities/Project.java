package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "projects_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "activated", nullable = false)
  private Boolean activated = false;

  @Column(name = "automatic", nullable = false, columnDefinition = "boolean default false")
  private Boolean automatic = false;

  @Column(name = "username", nullable = false, columnDefinition = "varchar(255) default ''")
  private String username;

  @Column(name = "mv_max_per_table", nullable = false, columnDefinition = "numeric default 20")
  private Integer mvMaxPerTable = 20;

  @Column(name = "analysis_timeframe", nullable = false, columnDefinition = "numeric default 30")
  private Integer analysisTimeframe = 30;

  public Project() {}

  public Project(String projectId, Boolean activated, String username) {
    this.projectId = projectId;
    this.activated = activated;
    this.username = username;
  }

  public Long getId() {
    return id;
  }

  public String getProjectId() {
    return projectId;
  }

  public Boolean isActivated() {
    return activated;
  }

  public Boolean isAutomatic() {
    return automatic;
  }

  public Boolean setAutomatic(Boolean automatic) {
    if (automatic != null) {
      this.automatic = automatic;
      return true;
    }
    return false;
  }

  public void setActivated(Boolean activated) {
    if (activated != null) {
      this.activated = activated;
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    if (username != null) {
      this.username = username;
    }
  }

  public Integer getMvMaxPerTable() {
    return this.mvMaxPerTable;
  }

  public void setMvMaxPerTable(Integer mvMaxPerTable) {
    if (mvMaxPerTable != null) {
      this.mvMaxPerTable = mvMaxPerTable;
    }
  }

  public Integer getAnalysisTimeframe() {
    return this.analysisTimeframe;
  }

  public void setAnalysisTimeframe(Integer analysisTimeframe) {
    if (analysisTimeframe != null) {
      this.analysisTimeframe = analysisTimeframe;
    }
  }
}
