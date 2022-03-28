package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Project;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AColumnTest {

  private final String customColumnId = "myColumnId";
  private final String columnName = "myCol";
  private final String columnType = "INTEGER";
  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final String tableId = "myTableId";
  private final Project project = new Project(projectId);
  private final ADataset dataset = new ADataset(project, datasetName);
  private final ATable table = new ATable(tableId, project, dataset, tableName);

  @Test
  public void simpleValidationConstructor1() {
    AColumn column = new AColumn(table, columnName, columnType);
    Assert.assertEquals("myProjectId:myDataset.myTable#myCol", column.getId());
    Assert.assertEquals(columnName, column.getName());
    Assert.assertEquals(columnType, column.getType());
    Assert.assertEquals(project.getProjectId(), column.getTable().getProject().getProjectId());
    Assert.assertEquals(dataset.getDatasetName(), column.getTable().getDataset().getDatasetName());
    Assert.assertEquals(tableName, column.getTable().getTableName());
  }

  @Test
  public void simpleValidationConstructor2() {
    AColumn column = new AColumn(customColumnId, table, columnName, columnType);
    Assert.assertEquals(customColumnId, column.getId());
    Assert.assertEquals(columnName, column.getName());
    Assert.assertEquals(columnType, column.getType());
    Assert.assertEquals(project.getProjectId(), column.getTable().getProject().getProjectId());
    Assert.assertEquals(dataset.getDatasetName(), column.getTable().getDataset().getDatasetName());
    Assert.assertEquals(tableName, column.getTable().getTableName());
  }
}
