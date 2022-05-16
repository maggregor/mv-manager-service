package com.achilio.mvm.service.entities;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@Setter
@Table(name = "tables")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
@DiscriminatorColumn(name = "source", discriminatorType = DiscriminatorType.STRING)
public abstract class ATable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "tables_columns",
      joinColumns = @JoinColumn(name = "table_id"),
      inverseJoinColumns = @JoinColumn(name = "columns_id"))
  private List<AColumn> columns;

  @Column
  private String projectId;
  @Column
  private String datasetName;

  @Column
  private String tableName;

  @Column(unique = true)
  private String tableId;

  @Formula("(SELECT COUNT(*) FROM query_table_id q WHERE q.tables = table_ids)")
  private int queryCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private TableType type;

  protected void setTableId() {
    this.tableId =
        String.format(
            "%s.%s.%s", this.getProjectId(), this.getDatasetName(), this.getTableName());
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

  public abstract Float getCost();

  public enum TableType {
    TABLE,
    EXTERNAL_TABLE,
    VIEW,
    MATERIALIZED_VIEW,
    UNKNOWN,
  }
}
