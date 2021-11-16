package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.entities.DatasetMetadata;
import com.achilio.mvm.service.entities.ProjectMetadata;
import com.achilio.mvm.service.entities.TableMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableMetadataTest {

  @Test
  public void constructorEmpty() {
    TableMetadata tableMetadata = new TableMetadata();
    assertNull(tableMetadata.getProjectMetadata());
    assertNull(tableMetadata.getDatasetMetadata());
    assertNull(tableMetadata.getId());
  }

  @Test
  public void constructorWithArguments() {
    final ProjectMetadata projectMetadata = mock(ProjectMetadata.class);
    final DatasetMetadata datasetMetadata = mock(DatasetMetadata.class);
    final boolean enabled = true;
    TableMetadata tableMetadata = new TableMetadata(projectMetadata, datasetMetadata, enabled);
    assertEquals(projectMetadata, tableMetadata.getProjectMetadata());
    assertEquals(datasetMetadata, tableMetadata.getDatasetMetadata());
    assertTrue(enabled);
  }
}
