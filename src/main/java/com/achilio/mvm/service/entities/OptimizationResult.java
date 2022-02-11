package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
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

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private Optimization optimization;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "table_name", nullable = false)
  private String tableName;

  @Column(name = "statement", nullable = false, length = 65536)
  private String statement;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @Column(name = "totalProcessedBytes")
  private long totalProcessedBytes;

  @Column(name = "queries")
  private int queries;

  public OptimizationResult(
      final Optimization optimization,
      final FetchedTable referenceTable,
      final String statement) {
    this(optimization, referenceTable, statement, null);

  }

  public OptimizationResult(
      final Optimization optimization,
      final FetchedTable referenceTable,
      final String statement,
      final QueryUsageStatistics statistics) {
    this.optimization = optimization;
    this.projectId = referenceTable.getProjectId();
    this.datasetName = referenceTable.getDatasetName();
    this.tableName = referenceTable.getTableName();
    this.statement = statement;
    if (statistics != null) {
      this.totalProcessedBytes = statistics.getProcessedBytes();
      this.queries = statistics.getQueryCount();
    }
  }

  public OptimizationResult() {
  }

  public Long getId() {
    return id;
  }

  public String getStatement() {
    return this.statement;
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

  public long getTotalProcessedBytes() {
    return this.totalProcessedBytes;
  }

  public int getQueries() {
    return this.queries;
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

  public void setStatusIfEmpty(Status status) {
    if (this.status == null) {
      this.status = status;
    }
  }

  public enum Status {
    APPLY,
    LIMIT_REACHED_PER_TABLE,
    LIMIT_REACHED_PER_PROJECT,
    PLAN_LIMIT_REACHED
  }

}
