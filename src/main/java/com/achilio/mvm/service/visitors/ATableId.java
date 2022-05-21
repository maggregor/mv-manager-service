package com.achilio.mvm.service.visitors;

import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a TableId path
 */
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class ATableId {

  @Column
  private String projectId;

  @Column
  private String datasetName;

  @Column
  private String tableName;

  private ATableId(String project, String dataset, String table) {
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(dataset), "Provided dataset is null or empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(table), "Provided table is null or empty");
    this.projectId = project;
    this.datasetName = dataset;
    this.tableName = table;
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

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Transient
  public String getTableId() {
    Preconditions.checkNotNull(projectId);
    Preconditions.checkNotNull(datasetName);
    Preconditions.checkNotNull(tableName);
    return String.format("%s.%s.%s", projectId, datasetName, tableName);
  }

  public String asPath() {
    StringJoiner joiner = new StringJoiner(".");
    if (StringUtils.isNotEmpty(projectId)) {
      joiner.add(projectId);
    }
    joiner.add(datasetName);
    joiner.add(tableName);
    return joiner.toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(projectId).append(datasetName).append(tableName)
        .toHashCode();
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
        .append(projectId, tableId.projectId)
        .append(datasetName, tableId.datasetName)
        .append(tableName, tableId.tableName)
        .isEquals();
  }

  @Override
  public String toString() {
    return "TableId{"
        + "project='"
        + projectId
        + '\''
        + ", dataset='"
        + datasetName
        + '\''
        + ", table='"
        + tableName
        + '\''
        + '}';
  }
}
