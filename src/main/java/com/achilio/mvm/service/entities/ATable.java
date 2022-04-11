package com.achilio.mvm.service.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@Table(name = "tables")
public class ATable {

  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  ADataset dataset;

  @ManyToOne Project project;

  @OneToMany(mappedBy = "table", fetch = FetchType.EAGER)
  List<AColumn> columns;

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

  @Override
  public int hashCode() {
    return tableId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ATable aTable = (ATable) o;

    return tableId.equals(aTable.tableId);
  }

  public String getDatasetName() {
    return this.dataset.getDatasetName();
  }

  public String getProjectId() {
    return this.project.getProjectId();
  }
}
