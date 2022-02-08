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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "events")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class OptimizationEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private Optimization optimization;

  @CreatedDate
  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private StatusType status;

  public OptimizationEvent() {
  }

  public OptimizationEvent(Optimization optimization, StatusType statusType) {
    this.optimization = optimization;
    this.status = statusType;
  }

  public Long getId() {
    return id;
  }

  public StatusType getStatusType() {
    return this.status;
  }

  public Optimization getOptimization() {
    return this.optimization;
  }

  public enum StatusType {
    // Retrieve queries from history
    FETCHING_QUERIES,
    // Retrieve tables
    FETCHING_TABLES,
    // Keep the eligible queries
    FILTER_ELIGIBLE_QUERIES,
    // Keep queries from dataset
    FILTER_QUERIES_FROM_DATASET,
    // Extract columns, functions and expression from queries
    EXTRACTING_FIELD_SETS,
    // Merging field sets statistics
    MERGING_FIELD_SETS,
    // Optimizing
    OPTIMIZING_FIELD_SETS,
    // Materialized view statements building
    BUILD_MATERIALIZED_VIEWS_STATEMENT,
    // Publishing on pub/sub system
    PUBLISHING,
    // Publishing done
    PUBLISHED,
    ;
  }
}
