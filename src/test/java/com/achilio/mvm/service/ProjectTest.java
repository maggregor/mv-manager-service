package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectTest {

  @Test
  public void simpleValidation() {
    Project project = new Project("myProject");
    assertEquals("myProject", project.getProjectId());
    project.setActivated(true);
    assertTrue(project.isActivated());
    project.setActivated(false);
    assertFalse(project.isActivated());
    assertFalse(project.isAutomatic());
    project.setAutomatic(true);
    assertTrue(project.isAutomatic());
    project.setUsername("myUser");
    assertEquals("myUser", project.getUsername());
  }

  @Test
  public void simpleValidationNull() {
    Project project = new Project();
    assertNull(project.getProjectId());
    assertNull(project.getId());
  }

  @Test
  public void projectPlanSettingsSimpleValidation() {
    Project project = new Project();
    assertEquals(false, project.getAutomaticAvailable());
    project.setAutomaticAvailable(true);
    assertEquals(true, project.getAutomaticAvailable());
    project.setAutomaticAvailable(false);
    assertEquals(false, project.getAutomaticAvailable());
    project.setAutomaticAvailable(null);
    assertEquals(false, project.getAutomaticAvailable());

    assertEquals(Integer.valueOf(20), project.getMvMaxPerTableLimit());
    project.setMvMaxPerTableLimit(15);
    assertEquals(Integer.valueOf(15), project.getMvMaxPerTableLimit());
    project.setMvMaxPerTableLimit(null);
    assertEquals(Integer.valueOf(15), project.getMvMaxPerTableLimit());
  }
}
