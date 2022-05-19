package com.achilio.mvm.service.visitors;

import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a TableId path
 */
@Getter
@Setter
public class ATableId {

  private String projectId;
  private String datasetName;
  private String tableName;

  private ATableId(String projectId, String datasetName, String tableName) {
    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(datasetName), "Provided dataset is null or empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(tableName),
        "Provided table is null or empty");
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.tableName = tableName;
  }

  public static ATableId of(String projectId, String datasetName, String tableName) {
    return new ATableId(projectId, datasetName, tableName);
  }

  public static ATableId of(String datasetName, String tableName) {
    return new ATableId(null, datasetName, tableName);
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
