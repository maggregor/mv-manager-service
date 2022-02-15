package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.entities.ProjectMetadata;
import com.achilio.mvm.service.entities.TableMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableMetadataTest {

  @Test
  public void simpleValidation() {
    final ProjectMetadata projectMetadata = mock(ProjectMetadata.class);
    final boolean enabled = true;
    TableMetadata tableMetadata = new TableMetadata(projectMetadata, enabled);
    assertEquals(projectMetadata, tableMetadata.getProjectMetadata());
    assertTrue(tableMetadata.isEnabled());
  }

  @Test
  public void simpleValidationNull() {
    TableMetadata tableMetadata = new TableMetadata();
    assertNull(tableMetadata.getProjectMetadata());
    assertNull(tableMetadata.getId());
    assertFalse(tableMetadata.isEnabled());
  }
}
