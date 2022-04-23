package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.workflows.BigQueryDatasetProcessor;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.ViewDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryDatasetProcessorTest {

  private static final String PROJECT_NAME = "myProject";
  private static final String DATASET_NAME = "myDataset";
  private final List<Table> TABLES_MOCK = new ArrayList<>();

  @InjectMocks
  private BigQueryDatasetProcessor processor;
  @Mock
  private FetcherService service;

  private static Table simpleTableMock(String name) {
    Table table = mock(Table.class);
    when(table.exists()).thenReturn(true);
    when(table.getTableId()).thenReturn(TableId.of(PROJECT_NAME, DATASET_NAME, name));
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

  @Before
  public void setup() {
    Table table1 = simpleTableMock("myTable1");
    Table table2 = simpleTableMock("myTable2");
    Table table3 = simpleTableMock("myTable3");
    Table table4 = simpleTableMock("myTable4");
    when(table4.exists()).thenReturn(false);
    Table table5 = simpleTableMock("myTable5");
    when(table5.getDefinition()).thenReturn(mock(ViewDefinition.class));
    TABLES_MOCK.add(table1);
    TABLES_MOCK.add(table2);
    TABLES_MOCK.add(table3);
    TABLES_MOCK.add(table4);
    TABLES_MOCK.add(table5);
    when(service.fetchAllTables(PROJECT_NAME, DATASET_NAME)).thenReturn(TABLES_MOCK.stream());
  }

  @Test
  public void tableToATable() {
    Dataset dataset = simpleDatasetMock();
    ADataset aDataset = processor.process(dataset);
    assertDatasetAsADataset(dataset, aDataset);
    assertNotNull(aDataset.getATables());
    assertEquals(3, aDataset.getATables().size());
    List<ATable> aTables = aDataset.getATables();
    assertTableAsATable(TABLES_MOCK.get(0), aTables.get(0));
    assertTableAsATable(TABLES_MOCK.get(1), aTables.get(1));
    assertTableAsATable(TABLES_MOCK.get(2), aTables.get(2));
  }

  @Test
  public void allColumns() {
    String projectId = "myProject";
    String datasetName = "myDatasetForColumnsTesting";
    Dataset dataset = simpleDatasetMock();
    when(dataset.getDatasetId()).thenReturn(DatasetId.of(projectId, datasetName));
    Table table = simpleTableMock("myTestTable");
    List<Field> fields = new ArrayList<>();
    fields.add(Field.of("col_1", StandardSQLTypeName.BOOL));
    fields.add(Field.of("col_2", StandardSQLTypeName.INT64));
    fields.add(Field.of("col_3", StandardSQLTypeName.FLOAT64));
    fields.add(Field.of("col_4", StandardSQLTypeName.NUMERIC));
    fields.add(Field.of("col_5", StandardSQLTypeName.BIGNUMERIC));
    fields.add(Field.of("col_6", StandardSQLTypeName.STRING));
    fields.add(Field.of("col_7", StandardSQLTypeName.BYTES));
    fields.add(Field.of("col_8", StandardSQLTypeName.TIMESTAMP));
    fields.add(Field.of("col_9", StandardSQLTypeName.DATE));
    fields.add(Field.of("col_10", StandardSQLTypeName.TIME));
    fields.add(Field.of("col_11", StandardSQLTypeName.DATETIME));
    fields.add(Field.of("col_12", StandardSQLTypeName.GEOGRAPHY));
    // Non-supported STRUCT - Ignoring mapping
    // fields.add(Field.of("col_XX", StandardSQLTypeName.STRUCT));
    // Non-supported ARRAY - Ignoring mapping
    // fields.add(Field.of("col_8", StandardSQLTypeName.ARRAY));
    Schema schema = mock(Schema.class);
    StandardTableDefinition tableDefinition = mock(StandardTableDefinition.class);
    when(schema.getFields()).thenReturn(FieldList.of(fields));
    when(tableDefinition.getSchema()).thenReturn(schema);
    when(table.getDefinition()).thenReturn(tableDefinition);
    when(service.fetchAllTables(projectId, datasetName)).thenReturn(Stream.of(table));
    ADataset aDataset = processor.process(dataset);
    assertDatasetAsADataset(dataset, aDataset);
    assertNotNull(aDataset.getATables());
    assertFalse(aDataset.getATables().isEmpty());
    ATable aTable = aDataset.getATables().get(0);
    List<AColumn> columns = aTable.getColumns();
    assertEquals(fields.size(), columns.size());
    assertEquals(columns.get(0).getType(), "TYPE_BOOL");
    assertEquals(columns.get(1).getType(), "TYPE_UINT64");
    assertEquals(columns.get(2).getType(), "TYPE_FLOAT");
    assertEquals(columns.get(3).getType(), "TYPE_NUMERIC");
    assertEquals(columns.get(4).getType(), "TYPE_BIGNUMERIC");
    assertEquals(columns.get(5).getType(), "TYPE_STRING");
    assertEquals(columns.get(6).getType(), "TYPE_BYTES");
    assertEquals(columns.get(7).getType(), "TYPE_TIMESTAMP");
    assertEquals(columns.get(8).getType(), "TYPE_DATE");
    assertEquals(columns.get(9).getType(), "TYPE_TIME");
    assertEquals(columns.get(10).getType(), "TYPE_DATETIME");
    assertEquals(columns.get(11).getType(), "TYPE_GEOGRAPHY");
  }

  private void assertDatasetAsADataset(Dataset expected, ADataset actual) {
    assertNotNull(actual);
    assertNotNull(actual.getProjectId());
    assertEquals(expected.getDatasetId().getProject(), actual.getProjectId());
    assertEquals(expected.getDatasetId().getDataset(), actual.getDatasetName());
  }

  public void assertTableAsATable(Table expected, ATable actual) {
    assertEquals(expected.getTableId().getProject(), actual.getProjectId());
    assertEquals(expected.getTableId().getDataset(), actual.getDatasetName());
    assertEquals(expected.getTableId().getTable(), actual.getTableName());
    List<Field> fields = expected.getDefinition().getSchema().getFields();
    for (int i = 0; i < fields.size(); i++) {
      AColumn actualColumn = actual.getColumns().get(i);
      assertEquals(fields.get(i).getName(), actualColumn.getName());
      assertNotNull(actualColumn.getType());
    }
  }

  private Dataset simpleDatasetMock() {
    Dataset dataset = mock(Dataset.class);
    when(dataset.getDatasetId()).thenReturn(DatasetId.of(PROJECT_NAME, DATASET_NAME));
    return dataset;
  }
}