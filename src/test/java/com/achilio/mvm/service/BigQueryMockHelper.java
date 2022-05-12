package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import java.util.ArrayList;
import java.util.List;

public class BigQueryMockHelper {


  public static Table simpleTableMock(String name) {
    return simpleTableMock("myProject", "myDataset", name);
  }

  public static Table simpleTableMock(String project, String dataset, String name) {
    Table table = mock(Table.class);
    when(table.getTableId()).thenReturn(TableId.of(project, dataset, name));
    StandardTableDefinition tableDefinition = mock(StandardTableDefinition.class);
    Schema schema = mock(Schema.class);
    List<Field> fields = new ArrayList<>();
    fields.add(Field.of("col1", LegacySQLTypeName.BOOLEAN));
    fields.add(Field.of("col2", LegacySQLTypeName.FLOAT));
    FieldList fieldList = FieldList.of(fields);
    when(schema.getFields()).thenReturn(fieldList);
    when(tableDefinition.getSchema()).thenReturn(schema);
    when(table.getDefinition()).thenReturn(tableDefinition);
    return table;
  }


}
