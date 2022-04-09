package com.achilio.mvm.service.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Setter
@Getter
@Entity
@Table(name = "fetcher_jobs")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Job {
  private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private Date createdAt;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private JobStatus status;

  protected Job() {}

  public Job(String projectId) {
    this.projectId = projectId;
    this.status = JobStatus.PENDING;
  }

  public void setStatus(JobStatus status) {
    LOGGER.info("Job {} set to {}", this.getId(), status);
    this.status = status;
  }

  public enum JobStatus {
    PENDING,
    WORKING,
    FINISHED,
    ERROR
  }
}
