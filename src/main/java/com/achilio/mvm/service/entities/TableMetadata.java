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
@Table(name = "tables_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class TableMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private ProjectMetadata projectMetadata;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  public TableMetadata() {
  }

  public TableMetadata(ProjectMetadata projectMetadata,
      boolean enabled) {
    this.projectMetadata = projectMetadata;
    this.enabled = enabled;
  }

  public Long getId() {
    return id;
  }

  public ProjectMetadata getProjectMetadata() {
    return projectMetadata;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
