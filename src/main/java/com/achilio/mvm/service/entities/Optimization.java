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

  @Column(name = "query_eligible_percentage")
  private Double queryEligiblePercentage;

  @Column(name = "mv_max_table")
  private Integer mvMaxPerTable;

  @Column(name = "mv_max_plan")
  private Integer mvMaxPlan;

  @Column(name = "mv_applied_count")
  private Integer mvAppliedCount;

  @Column(name = "mv_proposal_count")
  private Integer mvProposalCount;

  @Column(name = "username", nullable = false, columnDefinition = "varchar(255) default 'unknown'")
  private String username;

  public Optimization() {}

  public Optimization(String projectId, String username) {
    this.projectId = projectId;
    this.username = username;
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

  public Integer getMvMaxPerTable() {
    return this.mvMaxPerTable;
  }

  public void setMvMaxTable(Integer mvMaxPerTable) {
    this.mvMaxPerTable = mvMaxPerTable;
  }

  public Integer getMvMaxPlan() {
    return this.mvMaxPlan;
  }

  public void setMvMaxPlan(Integer mvMaxPlan) {
    this.mvMaxPlan = mvMaxPlan;
  }

  public Integer getMvAppliedCount() {
    return this.mvAppliedCount;
  }

  public void setMvAppliedCount(Integer mvAppliedCount) {
    this.mvAppliedCount = mvAppliedCount;
  }

  public Integer getMvProposalCount() {
    return this.mvProposalCount;
  }

  public void setMvProposalCount(Integer mvProposalCount) {
    this.mvProposalCount = mvProposalCount;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
