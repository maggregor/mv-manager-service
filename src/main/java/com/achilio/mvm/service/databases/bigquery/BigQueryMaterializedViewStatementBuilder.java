package com.achilio.mvm.service.databases.bigquery;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.visitors.ATableId;
import com.google.common.base.Preconditions;
import java.util.StringJoiner;

public class BigQueryMaterializedViewStatementBuilder implements MaterializedViewStatementBuilder {

  private static final String SEP_SQL_VERBS = " ";
  private static final String SEP_COLUMNS = ", ";
  private static final String SQL_VERB_SELECT = "SELECT";
  private static final String SQL_VERB_AS = "AS";
  private static final String SQL_VERB_FROM = "FROM";
  private static final String SQL_VERB_GROUP_BY = "GROUP BY";

  @Override
  public String build(QueryPattern fieldSet) {
    final StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
    joiner.add(buildSelect(fieldSet));
    joiner.add(buildFrom(fieldSet));
    if (!fieldSet.references().isEmpty() || !fieldSet.functions().isEmpty()) {
      joiner.add(buildGroupBy(fieldSet));
    }
    return joiner.toString();
  }

  private String serializeFieldWithAlias(Field field) {
    StringJoiner aliasJoiner = new StringJoiner(SEP_SQL_VERBS);
    aliasJoiner.add(field.getExpression());
    aliasJoiner.add(SQL_VERB_AS);
    aliasJoiner.add(field.getAlias());
    return aliasJoiner.toString();
  }

  private String serializeAlias(Field field) {
    return field.getAlias();
  }

  private String buildSelect(QueryPattern fieldSet) {
    StringBuilder builder = new StringBuilder();
    builder.append(SQL_VERB_SELECT);
    builder.append(SEP_SQL_VERBS);
    StringJoiner columns = new StringJoiner(SEP_COLUMNS);
    fieldSet.references().forEach(field -> columns.add(serializeFieldWithAlias(field)));
    fieldSet.functions().forEach(field -> columns.add(serializeFieldWithAlias(field)));
    if (!fieldSet.aggregates().isEmpty()) {
      // Add separator if there is aggregates.
      fieldSet.aggregates().forEach(field -> columns.add(serializeFieldWithAlias(field)));
    }
    builder.append(columns);
    return builder.toString();
  }

  private String buildFrom(QueryPattern fieldSet) {
    StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
    joiner.add(SQL_VERB_FROM);
    joiner.add(buildTableReference(fieldSet));
    return joiner.toString();
  }

  private String buildGroupBy(QueryPattern fieldSet) {
    StringBuilder builder = new StringBuilder();
    builder.append(SQL_VERB_GROUP_BY);
    builder.append(SEP_SQL_VERBS);
    StringJoiner columns = new StringJoiner(SEP_COLUMNS);
    fieldSet.references().forEach(field -> columns.add(serializeAlias(field)));
    fieldSet.functions().forEach(field -> columns.add(serializeAlias(field)));
    builder.append(columns);
    return builder.toString();
  }

  private String buildTableReference(QueryPattern fieldSet) {
    final ATableId table = fieldSet.getMainTable().getTable();
    final String projectId = table.getProjectId();
    final String datasetName = table.getDatasetName();
    final String tableName = table.getTableName();
    Preconditions.checkNotNull(table, "Table is required");
    Preconditions.checkNotNull(projectId, "Project ID is required");
    Preconditions.checkNotNull(datasetName, "ADataset name is required");
    Preconditions.checkNotNull(tableName, "Table name is required.");
    return String.format("`%s`.`%s`.`%s`", projectId, datasetName, tableName);
  }
}
