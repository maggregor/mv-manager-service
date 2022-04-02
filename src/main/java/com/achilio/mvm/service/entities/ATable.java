package com.achilio.mvm.service.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ATable {

  @ManyToOne(cascade = CascadeType.REMOVE)
  ADataset dataset;

  @ManyToOne Project project;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column private String tableName;

  @ManyToOne private FetcherStructJob lastFetcherStructJob;

  @ManyToOne @JoinColumn private FetcherStructJob initialFetcherStructJob;

  @Column(unique = true)
  private String tableId;

  public ATable() {}

  public ATable(Project project, ADataset dataset, String tableName, FetcherStructJob job) {
    this.lastFetcherStructJob = job;
    this.initialFetcherStructJob = job;
    this.project = project;
    this.dataset = dataset;
    this.tableName = tableName;
    setTableId();
  }

  public ATable(Project project, ADataset dataset, String tableName) {
    this.project = project;
    this.dataset = dataset;
    this.tableName = tableName;
    setTableId();
  }

  private void setTableId() {
    this.tableId =
        String.format(
            "%s.%s.%s", this.project.getProjectId(), this.dataset.getDatasetName(), this.tableName);
  }
}
