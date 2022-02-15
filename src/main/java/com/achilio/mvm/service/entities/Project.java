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
  private Boolean activated;

  @Column(name = "automatic", nullable = false, columnDefinition = "boolean default false")
  private Boolean automatic;

  @Column(name = "username", nullable = false, columnDefinition = "varchar(255) default ''")
  private String username;

  public Project() {}

  public Project(String projectId, Boolean activated, Boolean automatic, String username) {
    this.projectId = projectId;
    this.activated = activated;
    this.automatic = automatic;
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

  public void setAutomatic(Boolean automatic) {
    if (automatic != null) {
      this.automatic = automatic;
    }
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
    this.username = username;
  }
}
