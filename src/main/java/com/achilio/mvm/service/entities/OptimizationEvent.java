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
    // Retrieve datasets and tables structures
    FETCHING_MODELS,
    // Keep the eligible queries
    FILTER_ELIGIBLE_QUERIES,
    // Keep fields from dataset
    FILTER_FIELDS_FROM_DATASET,
    // Extract columns, functions and expression from queries
    EXTRACTING_FIELDS,
    // Optimizing
    OPTIMIZING_FIELDS,
    // Materialized view statements building
    BUILDING_OPTIMIZATION,
    // Publishing on pub/sub system
    PUBLISHING,
    // Publishing done
    PUBLISHED,
    ;
  }
}
