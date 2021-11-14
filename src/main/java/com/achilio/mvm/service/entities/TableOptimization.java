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
@Table(name = "tables_optimization")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class TableOptimization {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "table_name", nullable = false)
  private String tableName;

  @CreatedDate
  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  public TableOptimization() {
  }

  public TableOptimization(
      final String projectId,
      final String datasetName,
      final String tableName) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.tableName = tableName;
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
}
