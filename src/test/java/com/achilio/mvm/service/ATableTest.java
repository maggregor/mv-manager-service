package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Project;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ATableTest {

  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final String tableId = "myTableId";
  private final Project project = new Project(projectId);
  private final ADataset dataset = new ADataset(project, datasetName);

  @Test
  public void simpleValidationConstructorTest() {
    ATable table = new ATable(tableId, project, dataset, tableName);
    Assert.assertEquals(tableId, table.getId());
    Assert.assertEquals(project.getProjectId(), table.getProject().getProjectId());
    Assert.assertEquals(dataset.getDatasetName(), table.getDataset().getDatasetName());
    Assert.assertEquals(tableName, table.getTableName());
  }
}
