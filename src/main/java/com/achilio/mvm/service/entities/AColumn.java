package com.achilio.mvm.service.entities;

import static java.lang.String.format;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "columns")
public class AColumn {

  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  ATable table;

  @Id private String id;

  @Column private String name;

  @Column private String type;

  public AColumn() {}

  public AColumn(ATable table, String name, String type) {
    this.table = table;
    this.name = name;
    this.type = type;
    setId(table, name);
  }

  public AColumn(String id, ATable table, String name, String type) {
    this.id = id;
    this.table = table;
    this.name = name;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(ATable table, String name) {
    this.id =
        format(
            "%s.%s.%s#%s",
            table.getProject().getProjectId(),
            table.getDataset().getDatasetName(),
            table.getTableName(),
            name);
  }

  public ATable getTable() {
    return table;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }
}
