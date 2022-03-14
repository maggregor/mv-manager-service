package com.achilio.mvm.service.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "fetcher_jobs")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class FetcherJob {
  private static Logger LOGGER = LoggerFactory.getLogger(FetcherJob.class);

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private FetcherJobStatus status;

  protected FetcherJob() {}

  public FetcherJob(String projectId) {
    this.projectId = projectId;
    this.status = FetcherJobStatus.PENDING;
  }

  public Long getId() {
    return id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getProjectId() {
    return projectId;
  }

  public FetcherJobStatus getStatus() {
    return status;
  }

  public void setStatus(FetcherJobStatus status) {
    LOGGER.info("FetcherJob {} set to {}", this.getId(), status);
    this.status = status;
  }

  public enum FetcherJobStatus {
    PENDING,
    WORKING,
    FINISHED,
    ERROR
  }
}
