package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "datasets_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Dataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(Dataset.class);

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne private Project project;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "activated", nullable = false)
  private Boolean activated = false;

  public Dataset() {}

  public Dataset(Project project, String datasetName) {
    this.project = project;
    this.datasetName = datasetName;
  }

  public Long getId() {
    return id;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(Boolean activated) {
    if (activated != null) {
      this.activated = activated;
      LOGGER.info(
          "Update dataset {} activated={}",
          String.format("%s.%s", project.getProjectId(), datasetName),
          activated);
    }
  }
}
