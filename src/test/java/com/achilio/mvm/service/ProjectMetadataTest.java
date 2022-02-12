package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.ProjectMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectMetadataTest {

  @Test
  public void simpleValidation() {
    ProjectMetadata projectMetadata = new ProjectMetadata("myProject", true, false);
    assertEquals("myProject", projectMetadata.getProjectId());
    assertTrue(projectMetadata.isActivated());
    projectMetadata.setActivated(false);
    assertFalse(projectMetadata.isActivated());
    assertFalse(projectMetadata.isAutomatic());
    projectMetadata.setAutomatic(true);
    assertTrue(projectMetadata.isAutomatic());
  }

  @Test
  public void simpleValidationNull() {
    ProjectMetadata projectMetadata = new ProjectMetadata();
    assertNull(projectMetadata.getProjectId());
    assertNull(projectMetadata.getId());
  }
}
