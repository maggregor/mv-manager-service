package com.achilio.mvm.service.entities;

import java.util.Date;
import java.util.Objects;
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
  private Type status;

  public OptimizationEvent() {
  }

  public OptimizationEvent(Optimization optimization, Type type) {
    this.optimization = optimization;
    this.status = type;
  }

  public Optimization getOptimization() {
    return optimization;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public Type getStatus() {
    return status;
  }

  public Long getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizationEvent that = (OptimizationEvent) o;
    return createdDate == that.createdDate
        && id.equals(that.id)
        && optimization.equals(that.optimization)
        && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, optimization, createdDate, status);
  }

  public enum Type {
    IN_PROGRESS,
    APPROVAL_PENDING,
    FINISHED,
    DECLINED,
    SENT,
    DEPLOYED,
    ERROR,
  }
}
