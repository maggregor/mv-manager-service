package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(
    name = "datasets",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "dataset_name"})})
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class ADataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(ADataset.class);

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique = true)
  private String datasetId;

  @ManyToOne private Project project;

  @ManyToOne private FetcherStructJob lastFetcherStructJob;

  @ManyToOne @JoinColumn private FetcherStructJob initialFetcherStructJob;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "activated", nullable = false)
  private Boolean activated = false;

  public ADataset() {}

  public ADataset(Project project, String datasetName) {
    this.project = project;
    this.datasetName = datasetName;
    this.datasetId = String.format("%s:%s", project.getProjectId(), datasetName);
  }

  public ADataset(FetcherStructJob lastFetcherStructJob, Project project, String datasetName) {
    this.lastFetcherStructJob = lastFetcherStructJob;
    this.initialFetcherStructJob = lastFetcherStructJob;
    this.project = project;
    this.datasetName = datasetName;
    this.datasetId = String.format("%s:%s", project.getProjectId(), datasetName);
  }

  public Long getId() {
    return id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public Project getProject() {
    return project;
  }

  public FetcherStructJob getLastFetcherStructJob() {
    return lastFetcherStructJob;
  }

  public void setLastFetcherStructJob(FetcherStructJob lastFetcherStructJob) {
    this.lastFetcherStructJob = lastFetcherStructJob;
  }

  public FetcherStructJob getInitialFetcherStructJob() {
    return initialFetcherStructJob;
  }

  public void setInitialFetcherStructJob(FetcherStructJob initialFetcherStructJob) {
    this.initialFetcherStructJob = initialFetcherStructJob;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(Boolean activated) {
    if (activated != null) {
      this.activated = activated;
      LOGGER.info("Update dataset {} activated={}", datasetId, activated);
    }
  }

  @Override
  public int hashCode() {
    return datasetId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ADataset aDataset = (ADataset) o;

    return datasetId.equals(aDataset.datasetId);
  }
}
