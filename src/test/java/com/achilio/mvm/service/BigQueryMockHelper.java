package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;

public class BigQueryMockHelper {

  public static Table simpleBigQueryTableMock(String project, String dataset, String name) {
    return tableMock(project, dataset, name, StandardTableDefinition.class);
  }

  public static Table simpleBigQueryViewMock(String project, String dataset, String name) {
    return tableMock(project, dataset, name, StandardTableDefinition.class);
  }

  public static Table simpleBigQueryMaterializedViewMock(String project, String dataset,
      String name) {
    return tableMock(project, dataset, name, StandardTableDefinition.class);
  }

  public static Table simpleBigQueryExternalMock(String project, String dataset, String name) {
    return tableMock(project, dataset, name, StandardTableDefinition.class);
  }

  private static Table tableMock(String project, String dataset, String name,
      Class<? extends TableDefinition> definitionClass) {
    Table table = mock(Table.class);
    when(table.getTableId()).thenReturn(TableId.of(project, dataset, name));
    TableDefinition tableDefinition = mock(definitionClass);
    Schema schema = mock(Schema.class);
    FieldList fieldList = FieldList.of();
    when(schema.getFields()).thenReturn(fieldList);
    when(tableDefinition.getSchema()).thenReturn(schema);
    when(table.getDefinition()).thenReturn(tableDefinition);
    return table;
  }


}
