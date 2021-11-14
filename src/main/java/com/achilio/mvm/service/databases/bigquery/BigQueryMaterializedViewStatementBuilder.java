package com.achilio.mvm.service.databases.bigquery;

import com.achilio.mvm.service.databases.MaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
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
  public String build(FieldSet fieldSet) {
    final StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
    joiner.add(buildSelect(fieldSet));
    joiner.add(buildFrom(fieldSet));
    if (!fieldSet.references().isEmpty() || !fieldSet.functions().isEmpty()) {
      joiner.add(buildGroupBy(fieldSet));
    }
    return joiner.toString();
  }

  public String serializeFieldWithAlias(Field field) {
    StringJoiner aliasJoiner = new StringJoiner(SEP_SQL_VERBS);
    aliasJoiner.add(field.name());
    aliasJoiner.add(SQL_VERB_AS);
    aliasJoiner.add(field.alias());
    return aliasJoiner.toString();
  }

  public String serializeAlias(Field field) {
    return field.alias();
  }

  public String buildSelect(FieldSet fieldSet) {
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

  public String buildFrom(FieldSet fieldSet) {
    StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
    joiner.add(SQL_VERB_FROM);
    joiner.add(buildTableReference(fieldSet));
    return joiner.toString();
  }

  public String buildGroupBy(FieldSet fieldSet) {
    StringBuilder builder = new StringBuilder();
    builder.append(SQL_VERB_GROUP_BY);
    builder.append(SEP_SQL_VERBS);
    StringJoiner columns = new StringJoiner(SEP_COLUMNS);
    fieldSet.references().forEach(field -> columns.add(serializeAlias(field)));
    fieldSet.functions().forEach(field -> columns.add(serializeAlias(field)));
    builder.append(columns);
    return builder.toString();
  }

  public String buildTableReference(FieldSet fieldSet) {
    final FetchedTable table = fieldSet.getReferenceTables().iterator().next();
    final String projectId = table.getProjectId();
    final String datasetName = table.getDatasetName();
    final String tableName = table.getTableName();
    Preconditions.checkNotNull(table, "Table is required");
    Preconditions.checkNotNull(projectId, "Project ID is required");
    Preconditions.checkNotNull(datasetName, "Dataset name is required");
    Preconditions.checkNotNull(tableName, "Table name is required.");
    return String.format("`%s`.`%s`.`%s`", projectId, datasetName, tableName);
  }
}
