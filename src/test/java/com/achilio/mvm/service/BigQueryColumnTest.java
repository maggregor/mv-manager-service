package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.BigQueryColumn;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryColumnTest extends AColumnTest {

  @Override
  protected AColumn createColumn(String projectId, String tableId, String name) {
    Field field = Field.of(name, LegacySQLTypeName.FLOAT);
    return new BigQueryColumn(projectId, tableId, field);
  }
}
