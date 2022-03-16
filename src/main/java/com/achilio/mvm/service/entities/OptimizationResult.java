package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "results")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class OptimizationResult {

  private static final String MV_NAME_PREFIX = "achilio_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne private Optimization optimization;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "table_name", nullable = false)
  private String tableName;

  @Column(name = "statement", nullable = false, length = 65536)
  private String statement;

  @Column(name = "mv_name", nullable = false, columnDefinition = "varchar(255) default 'undefined'")
  private String mvName;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "status",
      nullable = false,
      columnDefinition = "varchar(255) default 'PLAN_LIMIT_REACHED'")
  private Status status;

  @Column(name = "hits", nullable = false, columnDefinition = "integer default 0")
  private Integer hits;

  public OptimizationResult(
      final Optimization optimization, final ATableId referenceTable, final String statement) {
    this(optimization, referenceTable, statement, 0);
  }

  public OptimizationResult(
      final Optimization optimization,
      final ATableId referenceTable,
      final String statement,
      final int hits) {
    this.optimization = optimization;
    this.projectId = referenceTable.getProjectId();
    this.datasetName = referenceTable.getDatasetName();
    this.tableName = referenceTable.getTableName();
    this.status = Status.UNDEFINED;
    this.statement = statement;
    this.mvName = MV_NAME_PREFIX + Math.abs(statement.hashCode());
    this.hits = hits;
  }

  public OptimizationResult() {}

  public Long getId() {
    return id;
  }

  public String getStatement() {
    return this.statement;
  }

  public String getMvName() {
    return this.mvName;
  }

  public Optimization getOptimization() {
    return this.optimization;
  }

  public String getProjectId() {
    return this.projectId;
  }

  public String getTableId() {
    return this.datasetName + "." + this.tableName;
  }

  public Integer getHits() {
    return this.hits;
  }

  public String getDatasetName() {
    return this.datasetName;
  }

  public String getTableName() {
    return this.tableName;
  }

  public Status getStatus() {
    return this.status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public boolean hasUndefinedStatus() {
    return this.status == Status.UNDEFINED;
  }

  public void setStatusIfUndefined(Status status) {
    if (hasUndefinedStatus()) {
      this.status = status;
    }
  }

  public enum Status {
    APPLY,
    LIMIT_REACHED_PER_TABLE,
    PLAN_LIMIT_REACHED,
    UNDEFINED,
  }
}
