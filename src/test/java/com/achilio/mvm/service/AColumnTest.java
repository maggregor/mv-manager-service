package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AColumnTest {

  private final String columnName = "myCol";
  private final String columnType = "INTEGER";
  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final Project project = new Project(projectId);
  private final ADataset dataset = new ADataset(project, datasetName);
  private final ATable table = new ATable(project, dataset, tableName);
  private final FetcherStructJob job = new FetcherStructJob(projectId);

  @Test
  public void simpleValidationConstructor1() {
    AColumn column = new AColumn(job, table, columnName, columnType);
    assertEquals("myProjectId.myDataset.myTable#myCol", column.getColumnId());
    assertEquals(columnName, column.getName());
    assertEquals(columnType, column.getType());
    assertEquals(project.getProjectId(), column.getTable().getProject().getProjectId());
    assertEquals(dataset.getDatasetName(), column.getTable().getDataset().getDatasetName());
    assertEquals(tableName, column.getTable().getTableName());
    assertEquals(dataset.isActivated(), column.isDatasetActivated());
  }
}
