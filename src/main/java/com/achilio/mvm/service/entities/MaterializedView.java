package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
@Table(name = "materialized_views")
public class MaterializedView {

  public static final String MV_NAME_PREFIX = "achilio_mv";
  private static final String CREATE_PREFIX = "CREATE MATERIALIZED VIEW";
  private static final String SQL_VERB_AS = "AS";

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

  @Column(name = "last_updated_at")
  @UpdateTimestamp
  private Date lastUpdatedAt;

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
    this.projectId = referenceTable.getProjectId();
    this.datasetName = referenceTable.getDatasetName();
    this.tableName = referenceTable.getTableName();
    this.status = MVStatus.NOT_APPLIED;
    this.statusReason = MVStatusReason.WAITING_APPROVAL;
    this.statement = statement;
    this.statementHashCode = String.valueOf(Math.abs(statement.hashCode()));
    this.mvUniqueName =
        String.join("-", this.projectId, this.datasetName, this.tableName, this.statementHashCode);
    this.mvName = String.join("_", this.tableName, MV_NAME_PREFIX, this.statementHashCode);
    this.mvDisplayName = String.join("_", MV_NAME_PREFIX, this.statementHashCode);
    this.hits = hits;
  }

  public boolean isApplied() {
    return this.getStatus().equals(MVStatus.APPLIED) || this.getStatus().equals(MVStatus.OUTDATED);
  }

  public boolean isNotApplied() {
    return this.getStatus().equals(MVStatus.NOT_APPLIED);
  }

  public void setStatus(MVStatus status) {
    if (status.equals(MVStatus.APPLIED) || status.equals(MVStatus.OUTDATED)) {
      setStatusReason(null);
    }
    this.status = status;
  }

  public String generateCreateStatement() {
    String fullMvName =
        String.join(".", this.getDatasetName(), RandomStringUtils.randomAlphabetic(8));
    return String.join(" ", CREATE_PREFIX, fullMvName, SQL_VERB_AS, this.getStatement());
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
