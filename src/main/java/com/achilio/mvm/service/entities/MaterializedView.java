package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "materialized_views")
public class MaterializedView {

  private static final String MV_NAME_PREFIX = "achilio_mv_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(updatable = false)
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("initialJobId")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  private FindMVJob initialJob;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("lastJobId")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  private FindMVJob lastJob;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private Date createdAt;

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

  @Column(name = "mv_display_name")
  private String mvDisplayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private MVStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_reason")
  private MVStatusReason statusReason;

  @Column(name = "hits")
  private Integer hits;

  public MaterializedView(ATableId referenceTable, String statement) {
    this(null, referenceTable, statement, 0);
  }

  public MaterializedView(
      final FindMVJob job, final ATableId referenceTable, final String statement) {
    this(job, referenceTable, statement, 0);
  }

  public MaterializedView(
      final FindMVJob job,
      final ATableId referenceTable,
      final String statement,
      final Integer hits) {
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
    this.mvDisplayName = MV_NAME_PREFIX + this.statementHashCode;
    this.hits = hits;
  }

  public boolean isApplied() {
    return this.status.equals(MVStatus.APPLIED) || this.status.equals(MVStatus.OUTDATED);
  }

  public boolean isNotApplied() {
    return this.status.equals(MVStatus.NOT_APPLIED);
  }

  public void setStatus(MVStatus status) {
    if (status.equals(MVStatus.APPLIED) || status.equals(MVStatus.OUTDATED)) {
      setStatusReason(null);
    }
    this.status = status;
  }

  @Override
  public int hashCode() {
    return mvUniqueName.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MaterializedView that = (MaterializedView) o;

    return mvUniqueName.equals(that.mvUniqueName);
  }

  public enum MVStatus {
    APPLIED,
    NOT_APPLIED,
    OUTDATED,
    UNKNOWN,
  }

  public enum MVStatusReason {
    // When MVStatus is NOT_APPLIED, MVStatus adds the reason for it
    WAITING_APPROVAL,
    DELETED_BY_USER,
    ERROR,
    ERROR_DURING_DELETION,
    ERROR_DURING_CREATION,
  }
}
