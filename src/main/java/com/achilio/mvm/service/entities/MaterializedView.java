package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "materialized_views")
public class MaterializedView {

  private static final String MV_NAME_PREFIX = "achilio_mv_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne
  @JoinColumn(updatable = false)
  private FindMVJob initialJob;

  @ManyToOne private FindMVJob lastJob;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "table_name", nullable = false)
  private String tableName;

  @Column(name = "statement", nullable = false, columnDefinition = "text")
  private String statement;

  @Column(nullable = false)
  private String statementHashCode;

  @Column(nullable = false, unique = true)
  private String mvUniqueName;

  @Column(name = "mv_name", nullable = false)
  private String mvName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private MVStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "statusReason", nullable = false)
  private MVStatusReason statusReason;

  @Column(name = "hits", nullable = false, columnDefinition = "integer default 0")
  private int hits;

  public MaterializedView(
      final FindMVJob job, final ATableId referenceTable, final String statement) {
    this(job, referenceTable, statement, 0);
  }

  public MaterializedView(
      final FindMVJob job, final ATableId referenceTable, final String statement, final int hits) {
    this.initialJob = job;
    this.lastJob = job;
    this.projectId = referenceTable.getProject();
    this.datasetName = referenceTable.getDataset();
    this.tableName = referenceTable.getTable();
    this.status = MVStatus.NOT_APPLIED;
    this.statusReason = MVStatusReason.WAITING_APPROVAL;
    this.statement = statement;
    this.statementHashCode = String.valueOf(Math.abs(statement.hashCode()));
    this.mvUniqueName =
        String.join("-", this.projectId, this.datasetName, this.tableName, this.statementHashCode);
    this.mvName = this.tableName + "_" + MV_NAME_PREFIX + this.statementHashCode;
    this.hits = hits;
  }

  public enum MVStatus {
    APPLIED,
    NOT_APPLIED,
  }

  public enum MVStatusReason {
    // When MVStatus is NOT_APPLIED, MVStatus adds the reason for it
    WAITING_APPROVAL,
    DELETED,
    ERROR,
  }
}
