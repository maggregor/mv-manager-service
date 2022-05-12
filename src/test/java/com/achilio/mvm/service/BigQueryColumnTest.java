package com.achilio.mvm.service;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.BigQueryColumn;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.StandardSQLTypeName;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryColumnTest extends AColumnTest {

  @Override
  protected AColumn createColumn(String projectId, String tableId, String name) {
    Field field = Field.of(name, LegacySQLTypeName.FLOAT);
    return new BigQueryColumn(projectId, tableId, field);
  }

  @Test
  public void when_unsupportedColumnType_thenThrows() {
    List<Field> unsupportedFieldTypes = Collections.singletonList(
        Field.of("a", StandardSQLTypeName.STRUCT, Field.of("b", StandardSQLTypeName.BOOL)));
    unsupportedFieldTypes.forEach(f -> {
      Exception e = assertThrows(IllegalArgumentException.class,
          () -> new BigQueryColumn("col", "tid", f));
      assertTrue(e.getMessage().startsWith("Unsupported column type"));
    });
  }
}
