package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "datasets_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class DatasetMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne private ProjectMetadata projectMetadata;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "activated", nullable = false)
  private Boolean activated;

  public DatasetMetadata() {}

  public DatasetMetadata(ProjectMetadata projectMetadata, String datasetName, Boolean activated) {
    this.projectMetadata = projectMetadata;
    this.datasetName = datasetName;
    this.activated = activated;
  }

  public Long getId() {
    return id;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(Boolean activated) {
    this.activated = activated;
  }
}
