package com.achilio.mvm.service.visitors;

import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** Represents a TableId path */
public class ATableId {

  private final String project;
  private final String dataset;
  private final String table;
  private String tableId;

  private ATableId(String project, String dataset, String table) {
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(dataset), "Provided dataset is null or empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(table), "Provided table is null or empty");
    this.project = project;
    this.dataset = dataset;
    this.table = table;
    setTableId(project, dataset, table);
  }

  public static ATableId of(String project, String dataset, String table) {
    return new ATableId(project, dataset, table);
  }

  public static ATableId of(String dataset, String table) {
    return new ATableId(null, dataset, table);
  }

  public static ATableId parse(String token) {
    token = token.replaceAll("`", StringUtils.EMPTY);
    String[] split = token.split("\\.");
    if (split.length == 2) {
      return ATableId.of(split[0], split[1]);
    } else if (split.length == 3) {
      return ATableId.of(split[0], split[1], split[2]);
    }
    return null;
  }

  public static ATableId fromGoogleTableId(TableId t) {
    return ATableId.of(t.getProject(), t.getDataset(), t.getTable());
  }

  private void setTableId(String project, String dataset, String table) {
    this.tableId = String.format("%s.%s.%s", project, dataset, table);
  }

  public String asPath() {
    StringJoiner joiner = new StringJoiner(".");
    if (StringUtils.isNotEmpty(project)) {
      joiner.add(project);
    }
    joiner.add(dataset);
    joiner.add(table);
    return joiner.toString();
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

  public String getTableId() {
    return this.tableId;
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
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(project).append(dataset).append(table).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ATableId)) {
      return false;
    }

    ATableId tableId = (ATableId) o;

    return new EqualsBuilder()
        .append(project, tableId.project)
        .append(dataset, tableId.dataset)
        .append(table, tableId.table)
        .isEquals();
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
