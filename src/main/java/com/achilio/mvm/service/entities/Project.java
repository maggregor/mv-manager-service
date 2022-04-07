package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.databases.entities.FetchedProject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "projects")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Project {

  private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "project_id", nullable = false, unique = true)
  private String projectId;

  @Column(name = "project_name")
  private String projectName;

  @Column private String teamName;

  @Column(name = "activated", nullable = false)
  private Boolean activated = true;

  @Column(name = "automatic", nullable = false, columnDefinition = "boolean default false")
  private Boolean automatic = false;

  @Column(name = "username", nullable = false, columnDefinition = "varchar(255) default ''")
  private String username = Strings.EMPTY;

  @Column(name = "mv_max_per_table", nullable = false, columnDefinition = "numeric default 5")
  private Integer mvMaxPerTable = 5;

  @Column(name = "analysis_timeframe", nullable = false, columnDefinition = "numeric default 30")
  private Integer analysisTimeframe = 30;

  @ManyToOne private Connection connection;

  public Project() {}

  public Project(String projectId) {
    this(projectId, null, null);
  }

  public Project(String projectId, String projectName) {
    this(projectId, projectName, null);
  }

  public Project(String projectId, String projectName, String teamName) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.teamName = teamName;
  }

  public Project(FetchedProject fetchedProject) {
    this(fetchedProject.getProjectId(), fetchedProject.getName(), fetchedProject.getTeamName());
  }

  public Long getId() {
    return id;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
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
      LOGGER.info("ProjectId {}: Set automatic mode to {}", projectId, automatic);
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
      LOGGER.info("ProjectId {}: Set mvMaxPerTable to {}", projectId, mvMaxPerTable);
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

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }
}
