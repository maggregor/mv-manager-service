package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "projects_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Project {

  private static Logger LOGGER = LoggerFactory.getLogger(Project.class);

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
  private String username = Strings.EMPTY;

  @Column(name = "mv_max_per_table", nullable = false, columnDefinition = "numeric default 20")
  private Integer mvMaxPerTable = 20;

  @Column(name = "analysis_timeframe", nullable = false, columnDefinition = "numeric default 30")
  private Integer analysisTimeframe = 30;

  @Column(name = "stripe_customer_id")
  private String customerId;

  @Column(
      name = "mv_max_per_table_limit",
      nullable = false,
      columnDefinition = "numeric default 20")
  private Integer mvMaxPerTableLimit = 20;

  @Column(
      name = "automatic_available",
      nullable = false,
      columnDefinition = "boolean default false")
  private Boolean automaticAvailable = false;

  public Project() {}

  public Project(String projectId) {
    this.projectId = projectId;
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
      if (automatic && !this.automaticAvailable) {
        String errorMessage =
            "Cannot set project to automatic mode. Automatic mode is not available on this project";
        LOGGER.warn(errorMessage);
        throw new IllegalArgumentException(errorMessage);
      }
      this.automatic = automatic;
      LOGGER.info("Project {} set automatic mode to {}", projectId, automatic);
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
      if (mvMaxPerTable > mvMaxPerTableLimit) {
        String errorMessage =
            String.format(
                "Cannot set max MV per table to %s. Limit is %s",
                mvMaxPerTable, mvMaxPerTableLimit);
        LOGGER.warn(errorMessage);
        throw new IllegalArgumentException(errorMessage);
      }
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

  public String getCustomerId() {
    return this.customerId;
  }

  public void setCustomerId(String customerId) {
    if (customerId != null) {
      this.customerId = customerId;
    }
  }

  public Integer getMvMaxPerTableLimit() {
    return mvMaxPerTableLimit;
  }

  public void setMvMaxPerTableLimit(Integer mvMaxPerTableLimit) {
    // We don't automatically update the mvMaxPerTable field intentionally, in case we manually want
    // certain project to have special settings
    if (mvMaxPerTableLimit != null) {
      this.mvMaxPerTableLimit = mvMaxPerTableLimit;
    }
  }

  public Boolean getAutomaticAvailable() {
    return automaticAvailable;
  }

  public void setAutomaticAvailable(Boolean automaticAvailable) {
    // We don't automatically update the automatic field intentionally, in case we manually want
    // certain project to have special settings
    if (automaticAvailable != null) {
      this.automaticAvailable = automaticAvailable;
    }
  }
}
