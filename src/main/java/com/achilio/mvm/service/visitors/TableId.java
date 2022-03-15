package com.achilio.mvm.service.visitors;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** Represents a TableId path */
public class TableId {

  private final String project;
  private final String dataset;
  private final String table;

  private TableId(String project, String dataset, String table) {
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(dataset), "Provided dataset is null or empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(table), "Provided table is null or empty");
    this.project = project;
    this.dataset = dataset;
    this.table = table;
  }

  public static TableId of(String project, String dataset, String table) {
    return new TableId(Preconditions.checkNotNull(project), dataset, table);
  }

  public static TableId of(String dataset, String table) {
    return new TableId(null, dataset, table);
  }

  public static TableId parse(String token) {
    String[] split = token.split("\\.");
    if (split.length == 2) {
      return TableId.of(split[0], split[1]);
    } else if (split.length == 3) {
      return TableId.of(split[0], split[1], split[2]);
    }
    return null;
  }

  public String getProject() {
    return this.project;
  }

  public String getDataset() {
    return this.dataset;
  }

  public String getTable() {
    return this.table;
  }

  @Deprecated
  public String getProjectId() {
    return this.project;
  }

  @Deprecated
  public String getDatasetName() {
    return this.dataset;
  }

  @Deprecated
  public String getTableName() {
    return this.table;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TableId tableId = (TableId) o;

    return new EqualsBuilder()
        .append(project, tableId.project)
        .append(dataset, tableId.dataset)
        .append(table, tableId.table)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(project).append(dataset).append(table).toHashCode();
  }

  @Override
  public String toString() {
    return "TableId{"
        + "project='"
        + project
        + '\''
        + ", dataset='"
        + dataset
        + '\''
        + ", table='"
        + table
        + '\''
        + '}';
  }
}
