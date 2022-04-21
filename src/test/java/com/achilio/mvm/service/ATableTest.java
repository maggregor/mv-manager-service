package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ATableTest {

  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final String tableId = "myProjectId.myDataset.myTable";
  private final Project project = new Project(projectId);
  private final ADataset dataset = new ADataset(project, datasetName);

  @Test
  public void simpleValidationConstructor() {
    ATable table = new ATable(projectId, datasetName, tableName);
    assertEquals(tableId, table.getTableId());
    assertEquals(project.getProjectId(), table.getProjectId());
    assertEquals(dataset.getDatasetName(), table.getDatasetName());
    assertEquals(tableName, table.getTableName());
    assertEquals(projectId, table.getProjectId());
    assertEquals(datasetName, table.getDatasetName());
  }
}
