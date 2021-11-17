package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

  public OptimizationResult(
      final Optimization optimization,
      final FetchedTable referenceTable,
      final String statement) {
    this.optimization = optimization;
    this.projectId = referenceTable.getProjectId();
    this.datasetName = referenceTable.getDatasetName();
    this.tableName = referenceTable.getTableName();
    this.statement = statement;
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

  public String getDatasetName() {
    return this.datasetName;
  }

  public String getTableName() {
    return this.tableName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizationResult that = (OptimizationResult) o;
    return id.equals(that.id)
        && optimization.equals(that.optimization)
        && projectId.equals(that.projectId)
        && datasetName.equals(that.datasetName)
        && tableName.equals(that.tableName)
        && statement.equals(that.statement);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, optimization, projectId, datasetName, tableName, statement);
  }
}
