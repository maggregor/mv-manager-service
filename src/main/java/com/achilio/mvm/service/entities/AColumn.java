package com.achilio.mvm.service.entities;

import static java.lang.String.format;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "columns")
public class AColumn {

  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  ATable table;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique = true)
  private String columnId;

  @Column private String name;

  @Column private String type;

  public AColumn() {}

  public AColumn(ATable table, String name, String type) {
    this.table = table;
    this.name = name;
    this.type = type;
    setColumnId(table, name);
  }

  public void setColumnId(ATable table, String name) {
    this.columnId = format("%s#%s", table.getTableId(), name);
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
