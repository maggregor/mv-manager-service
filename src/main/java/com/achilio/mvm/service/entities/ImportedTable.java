package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
    name = "imported_tables",
    indexes = {@Index(name = "dataset_index", columnList = "dataset_id")})
public class ImportedTable {

  @ManyToOne Dataset dataset;
  @ManyToOne Project project;

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column private String tableName;

  public ImportedTable() {}

  public ImportedTable(String id, Project project, Dataset dataset, String tableName) {
    this.id = id;
    this.project = project;
    this.dataset = dataset;
    this.tableName = tableName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}
