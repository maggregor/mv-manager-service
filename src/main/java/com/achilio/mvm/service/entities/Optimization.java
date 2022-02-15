package com.achilio.mvm.service.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "optimizations")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Optimization {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @CreatedDate
  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "query_eligible_percentage", nullable = true)
  private Double queryEligiblePercentage;

  public Optimization() {
  }

  public Optimization(String projectId) {
    this.projectId = projectId;
  }

  public Long getId() {
    return id;
  }

  public String getProjectId() {
    return this.projectId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public Double getQueryEligiblePercentage() {
    return queryEligiblePercentage;
  }

  public void setQueryEligiblePercentage(Double queryEligiblePercentage) {
    this.queryEligiblePercentage = queryEligiblePercentage;
  }
}
