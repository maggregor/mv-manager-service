package com.achilio.mvm.service.entities;

import static java.lang.String.format;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "columns")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "source", discriminatorType = DiscriminatorType.STRING)
public abstract class AColumn {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique = true)
  private String columnId;

  @Column
  private String name;

  @Column
  private String type;

  @Column
  private String projectId;

  public AColumn() {
  }

  public AColumn(String projectId, String tableId, String name) {
    this.projectId = projectId;
    this.name = name;
    setColumnId(tableId, name);
  }

  public void setColumnId(String tableId, String name) {
    this.columnId = format("%s#%s", tableId, name);
  }

  @Override
  public int hashCode() {
    return columnId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AColumn aColumn = (AColumn) o;

    return columnId.equals(aColumn.columnId);
  }
}
