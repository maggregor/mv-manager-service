package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Project;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ATableTest {

  private final String tableName = "myTable";
  private final String projectId = "myProjectId";
  private final String datasetName = "myDataset";
  private final String tableId = "myProjectId.myDataset.myTable";
  private final Project project = new Project(projectId);
  private final ADataset dataset = new ADataset(project, datasetName);

  protected abstract ATable createTable(String projectId, String datasetName, String tableName,
      Long numRows, Date createdAt, Date lastModifiedAt);

  @Test
  public void simpleValidationConstructor() {
    Date createdAt = new Date(1652859682L);
    Date lastModifiedAt = new Date(1652859695L);
    ATable table = createTable(projectId, datasetName, tableName, 1000L, createdAt, lastModifiedAt);
    assertEquals(tableId, table.getTableId());
    assertEquals(project.getProjectId(), table.getProjectId());
    assertEquals(dataset.getDatasetName(), table.getDatasetName());
    assertEquals(tableName, table.getTableName());
    assertEquals(projectId, table.getProjectId());
    assertEquals(datasetName, table.getDatasetName());
    assertEquals(new Long(1000), table.getNumRows());
    assertEquals(createdAt, table.getCreatedAt());
    assertEquals(lastModifiedAt, table.getLastModifiedAt());
  }

}
