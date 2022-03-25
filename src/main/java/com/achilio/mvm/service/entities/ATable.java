package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
    name = "imported_tables"
    //    indexes = {@Index(name = "dataset_index", columnList = "dataset_id")}
    )
public class ATable {

  @ManyToOne ADataset dataset;
  @ManyToOne Project project;

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column private String tableName;

  public ATable() {}

  public ATable(String id, Project project, ADataset dataset, String tableName) {
    this.id = id;
    this.project = project;
    this.dataset = dataset;
    this.tableName = tableName;
  }

  public String getId() {
    return id;
  }

  public ADataset getDataset() {
    return dataset;
  }

  public Project getProject() {
    return project;
  }

  public String getTableName() {
    return tableName;
  }
}
