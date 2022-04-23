package com.achilio.mvm.service.entities;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@Setter
@Table(name = "tables")
public class ATable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<AColumn> columns;

  @Column
  private String projectId;
  @Column
  private String datasetName;

  @Column
  private String tableName;

  @Column(unique = true)
  private String tableId;

  @Formula("(SELECT COUNT(*) FROM query_table_id q WHERE q.tables = table_id)")
  private int queryCount;

  public ATable() {
  }

  public ATable(String projectId, String datasetName, String tableName) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.tableName = tableName;
    setTableId();
  }

  private void setTableId() {
    this.tableId =
        String.format(
            "%s.%s.%s", this.getProjectId(), this.getDatasetName(), this.tableName);
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
    return this.datasetName;
  }

  public String getProjectId() {
    return this.projectId;
  }
}
